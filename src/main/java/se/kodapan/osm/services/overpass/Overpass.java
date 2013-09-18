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


  /**
   * Will be removed in version 0.0.1
   * @deprecated use {@link OverpassUtils#loadAllObjects(se.kodapan.osm.domain.root.Root)}
   */
  @Deprecated
  public void loadAllObjects(Root root) throws OverpassException, OsmXmlParserException {
    new OverpassUtils(this).loadAllObjects(root);
  }


  /**
   * Will be removed in version 0.0.1
   * @deprecated use {@link OverpassUtils#getNode(long)}
   */
  @Deprecated
  public Node getNode(long id) throws Exception {
    return new OverpassUtils(this).getNode(id);
  }


  /**
   * Will be removed in version 0.0.1
   * @deprecated use {@link OverpassUtils#loadObjects(se.kodapan.osm.data.planet.parser.xml.instantiated.InstantiatedOsmXmlParser, java.util.Collection)}
   */
  @Deprecated
  public void loadObjects(InstantiatedOsmXmlParser parser, Collection<? extends OsmObject> osmObjects) throws OverpassException, OsmXmlParserException {
    new OverpassUtils(this).loadObjects(parser, osmObjects);
  }

  /**
   * Will be removed in version 0.0.1
   * @deprecated use {@link OverpassUtils#loadNode(se.kodapan.osm.data.planet.parser.xml.instantiated.InstantiatedOsmXmlParser, long)}
   */
  @Deprecated
  public Node loadNode(InstantiatedOsmXmlParser parser, long id) throws OverpassException, OsmXmlParserException {
    return new OverpassUtils(this).loadNode(parser, id);
  }

  /**
   * Will be removed in version 0.0.1
   * @deprecated use {@link OverpassUtils#loadWay(long)}
   */
  @Deprecated
  public Way loadWay(long id) throws OverpassException, OsmXmlParserException {
    return new OverpassUtils(this).loadWay(id);
  }

  /**
   * Will be removed in version 0.0.1
   * @deprecated use {@link OverpassUtils#loadWay(se.kodapan.osm.data.planet.parser.xml.instantiated.InstantiatedOsmXmlParser, long)}
   */
  @Deprecated
  public Way loadWay(InstantiatedOsmXmlParser parser, long id) throws OverpassException, OsmXmlParserException {
    return new OverpassUtils(this).loadWay(parser, id);
  }

  /**
   * Will be removed in version 0.0.1
   * @deprecated use {@link OverpassUtils#loadRelation(long)}
   */
  @Deprecated
  public Relation loadRelation(long id) throws OverpassException, OsmXmlParserException {
    return new OverpassUtils(this).loadRelation(id);
  }


  /**
   * Will be removed in version 0.0.1
   * @deprecated use {@link OverpassUtils#loadRelation(se.kodapan.osm.data.planet.parser.xml.instantiated.InstantiatedOsmXmlParser, long)}
   */
  @Deprecated
  public Relation loadRelation(InstantiatedOsmXmlParser parser, long id) throws OverpassException, OsmXmlParserException {
    return new OverpassUtils(this).loadRelation(parser, id);
  }


  /**
   * Will be removed in version 0.0.1
   * @deprecated use {@link OverpassUtils#loadEnvelope(se.kodapan.osm.data.planet.parser.xml.instantiated.InstantiatedOsmXmlParser, double, double, double, double)}
   */
  @Deprecated
  public void loadEnvelope(InstantiatedOsmXmlParser parser, double latitudeSouth, double longitudeWest, double latitudeNorth, double longitudeEast) throws OverpassException, OsmXmlParserException {
    new OverpassUtils(this).loadEnvelope(parser, latitudeSouth, longitudeWest, latitudeNorth, longitudeEast);
  }

}
