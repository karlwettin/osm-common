package se.kodapan.osm.services.overpass;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.osm.data.planet.parser.xml.OsmXmlParserException;
import se.kodapan.osm.data.planet.parser.xml.instantiated.InstantiatedOsmXmlParser;
import se.kodapan.osm.domain.*;
import se.kodapan.osm.domain.root.Root;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author kalle
 * @since 2012-12-31 16:32
 */
public class Overpass {

  private static Logger log = LoggerFactory.getLogger(Overpass.class);

  private ClientConnectionManager cm;
  private HttpClient httpClient;

  private static String defaultUserAgent = "Unnamed instance of " + Overpass.class.getName() + ", https://github.com/karlwettin/osm-common/";
  private String userAgent = defaultUserAgent;

  private String serverURL = "http://www.overpass-api.de/api/interpreter";

  public void open() throws Exception {
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

    cm = new ThreadSafeClientConnManager(schemeRegistry);
    httpClient = new DefaultHttpClient(cm);

  }

  public void close() throws Exception {

  }

  public String execute(String overpassQuery) throws OverpassException {
    return execute(overpassQuery, null);
  }

  /**
   * 2013-07-28 Usage policy accept 10 000 requests or 5GB data per day using up to two threads.
   * See http://wiki.openstreetmap.org/wiki/Overpass_API#Introduction
   *
   * @param overpassQuery
   * @param queryDescription
   * @return
   * @throws Exception
   */
  public String execute(String overpassQuery, String queryDescription) throws OverpassException {

    try {
    if (defaultUserAgent.equals(userAgent)) {
      throw new NullPointerException("Overpass HTTP header User-Agent not set!");
    }

    HttpPost post = new HttpPost(serverURL);
    post.setHeader("User-Agent", userAgent);

    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
    nameValuePairs.add(new BasicNameValuePair("data", overpassQuery));
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));


    log.debug("Executing overpass query: " + queryDescription + "\n" + overpassQuery);

    long started = System.currentTimeMillis();
    HttpResponse response = httpClient.execute(post);

    StringWriter buffer = new StringWriter();
    IOUtils.copy(new InputStreamReader(response.getEntity().getContent(), "utf8"), buffer);
    long ended = System.currentTimeMillis();

    if (log.isInfoEnabled()) {
      log.info("Overpass response for " + (queryDescription != null ? queryDescription : "un named query") + " was " + buffer.getBuffer().length() + " characters and received in " + (ended - started) + " ms.");
      if (log.isDebugEnabled()) {
        log.debug(buffer.getBuffer().toString());
      }
    }


    return buffer.toString();
    } catch (Exception e) {
      throw new OverpassException(e);
    }

  }

  public void loadAllObjects(Root root) throws OverpassException, OsmXmlParserException {

    InstantiatedOsmXmlParser parser = new InstantiatedOsmXmlParser();
    parser.setRoot(root);

    for (Node node : root.getNodes().values()) {
      if (!node.isLoaded()) {
        loadNode(parser, node.getId());
      }
    }
    for (Way way : root.getWays().values()) {
      if (!way.isLoaded()) {
        loadWay(parser, way.getId());
      }
    }
    for (Relation relation : root.getRelations().values()) {
      if (!relation.isLoaded()) {
        loadRelation(parser, relation.getId());
      }
    }
  }

  public Node getNode(long id) throws Exception {
    return loadNode(new InstantiatedOsmXmlParser(), id);
  }

  private static OsmObjectVisitor<String> getObjectType = new OsmObjectVisitor<String>() {
    @Override
    public String visit(Node node) {
      return "node";
    }

    @Override
    public String visit(Way way) {
      return "way";
    }

    @Override
    public String visit(Relation relation) {
      return "relation";
    }
  };

  public void loadObjects(InstantiatedOsmXmlParser parser, Collection<? extends OsmObject> osmObjects) throws OverpassException, OsmXmlParserException {

    StringWriter xml = new StringWriter(1000 + osmObjects.size() * 40);
    xml.write("<osm-script>\n");

    for (OsmObject osmObject : osmObjects) {
      xml.append("  <id-query ref=\"");
      xml.append("\" type=\"");
      xml.append(osmObject.accept(getObjectType));
      xml.append("\" />\n");
    }

    xml.append("  <print/>\n");
    xml.append("</osm-script>");

    String response = execute(xml.toString(), "Fetching " + osmObjects.size() + " objects by identity...");
    parser.parse(new StringReader(response));


  }

  public Node loadNode(InstantiatedOsmXmlParser parser, long id) throws OverpassException, OsmXmlParserException {

    String response = execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"node\"/>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting node " + id);

    parser.parse(new StringReader(response));
    return parser.getRoot().getNodes().get(id);

  }

  public Way loadWay(long id) throws OverpassException, OsmXmlParserException {
    return loadWay(new InstantiatedOsmXmlParser(), id);
  }

  public Way loadWay(InstantiatedOsmXmlParser parser, long id) throws OverpassException, OsmXmlParserException {

    parser.parse(new StringReader(execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"way\"/>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting way " + id)));

    parser.parse(new StringReader(execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"way\"/>\n" +
        "  <recurse type=\"way-node\"/>\n\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting way " + id)));


    return parser.getRoot().getWays().get(id);


  }

  public Relation loadRelation(long id) throws OverpassException, OsmXmlParserException {
    return loadRelation(new InstantiatedOsmXmlParser(), id);
  }


  public Relation loadRelation(InstantiatedOsmXmlParser parser, long id) throws OverpassException, OsmXmlParserException {


    parser.parse(new StringReader(execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"relation\"/>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting relation " + id)));

    parser.parse(new StringReader(execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"relation\"/>\n" +
        "  <recurse type=\"relation-way\"/>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting relation " + id + " ways")));

    parser.parse(new StringReader(execute("<osm-script>\n" +
        "  <union>\n" +
        "    <id-query ref=\"" + id + "\" type=\"relation\"/>\n" +
        "  <recurse type=\"relation-way\"/>\n" +
        "  <recurse type=\"way-node\"/>\n" +
        "  </union>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting relation nodes" + id)));


    return parser.getRoot().getRelations().get(id);

  }


  public String getServerURL() {
    return serverURL;
  }

  public void setServerURL(String serverURL) {
    this.serverURL = serverURL;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public void loadEnvelope(InstantiatedOsmXmlParser parser, double latitudeSouth, double longitudeWest, double latitudeNorth, double longitudeEast) throws OverpassException, OsmXmlParserException {
    DecimalFormat df = new DecimalFormat("#.#####################");
    String bbox = "<bbox-query s=\"" + df.format(latitudeSouth) + "\" n=\"" + df.format(latitudeNorth) + "\" w=\"" + df.format(longitudeWest) + "\" e=\"" + df.format(longitudeEast) + "\"/>";

    parser.parse(new StringReader(execute("<osm-script>\n" +
        "  " + bbox + "\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting nodes in bbox " + bbox)));

    parser.parse(new StringReader(execute("<osm-script>\n" +
        "  " + bbox + "\n" +
        "  <recurse type=\"node-way\"/>" +
        "  <print/>\n" +
        "</osm-script>", "Getting ways in bbox " + bbox)));

    parser.parse(new StringReader(execute("<osm-script>\n" +
        "  " + bbox + "\n" +
        "  <recurse type=\"node-relation\"/>" +
        "  <print/>\n" +
        "</osm-script>", "Getting relations in bbox " + bbox)));

    // todo make sure this really loads everything!
    System.currentTimeMillis();


  }

}
