package se.kodapan.osm.services.overpass;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.osm.data.planet.parser.xml.instantiated.InstantiatedOsmXmlParser;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.OsmObject;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.Root;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2012-12-31 16:32
 */
public class Overpass {

  private static Logger log = LoggerFactory.getLogger(Overpass.class);

  private HttpClient httpClient = new DefaultHttpClient();

  private String serverURL = "http://www.overpass-api.de/api/interpreter";
//  private String serverURL = "http://localhost:8765/api/interpreter";

  public void open() throws Exception {

  }

  public void close() throws Exception {

  }

  public String execute(String overpassQuery) throws Exception {
    return execute(overpassQuery, null);
  }

  public String execute(String overpassQuery, String queryDescription) throws Exception {

    HttpPost post = new HttpPost(serverURL);
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
    nameValuePairs.add(new BasicNameValuePair("data", overpassQuery.toString()));
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

    if (queryDescription != null) {
      log.debug("Executing overpass query: " + queryDescription);
    }
    long started = System.currentTimeMillis();
    HttpResponse response = httpClient.execute(post);

    StringWriter buffer = new StringWriter();
    IOUtils.copy(response.getEntity().getContent(), buffer);
    long ended = System.currentTimeMillis();
    log.debug("Overpass response received in " + (ended - started) + " ms.");

    return buffer.toString();

  }

  public void loadAllObjects(Root root) throws Exception {

    InstantiatedOsmXmlParser parser = new InstantiatedOsmXmlParser();
    parser.setRoot(root);

    for (Node node : root.getNodes().values()) {
      if (!node.isLoaded()) {
        getNode(parser, node.getId());
      }
    }
    for (Way way : root.getWays().values()) {
      if (!way.isLoaded()) {
        getWay(parser, way.getId());
      }
    }
    for (Relation relation : root.getRelations().values()) {
      if (!relation.isLoaded()) {
        getRelation(parser, relation.getId());
      }
    }
  }

  public Node getNode(long id) throws Exception {
    return getNode(new InstantiatedOsmXmlParser(), id);
  }

  public Node getNode(InstantiatedOsmXmlParser parser, long id) throws Exception {

    String response = execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"node\"/>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting node " + id);

    parser.parse(new StringReader(response));
    return parser.getRoot().getNodes().get(id);

  }

  public Way getWay(long id) throws Exception {
    return getWay(new InstantiatedOsmXmlParser(), id);
  }

  public Way getWay(InstantiatedOsmXmlParser parser, long id) throws Exception {

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

  public Relation getRelation(long id) throws Exception {
    return getRelation(new InstantiatedOsmXmlParser(), id);
  }


  public Relation getRelation(InstantiatedOsmXmlParser parser, long id) throws Exception {


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
        "    <recurse type=\"relation-way\"/>\n" +
        "    <recurse type=\"relation-node\"/>\n" +
        "    <recurse type=\"way-node\"/>\n" +
        "  </union>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting relation " + id)));


    return parser.getRoot().getRelations().get(id);

  }


  public String getServerURL() {
    return serverURL;
  }

  public void setServerURL(String serverURL) {
    this.serverURL = serverURL;
  }
}
