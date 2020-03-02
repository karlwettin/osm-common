package se.kodapan.osm.services.api.v_0_6;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import se.kodapan.osm.domain.*;
import se.kodapan.osm.domain.root.PojoRoot;
import se.kodapan.osm.domain.root.Root;
import se.kodapan.osm.parser.xml.instantiated.InstantiatedOsmXmlParser;
import se.kodapan.osm.parser.xml.instantiated.InstantiatedOsmXmlParserDelta;
import se.kodapan.osm.xml.OsmXmlWriter;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://wiki.openstreetmap.org/wiki/API_v0.6
 *
 * @author kalle
 * @since 2015-01-06 18:26
 */
public class ApiConnection {

  private static String defaultServerURL = "https://api.openstreetmap.org/api";
  private String serverURL;

  public ApiConnection() {
    this(defaultServerURL);
  }

  public ApiConnection(String serverURL) {
    while (serverURL.endsWith("/")) {
      serverURL = serverURL.substring(0, serverURL.length() - 1);
    }
    this.serverURL = serverURL + "/";
    prefix = this.serverURL + apiVersion + "/";

    httpClient = HttpClientBuilder.create()
        .setUserAgent("osm-common")
        .build();

  }

  private CloseableHttpClient httpClient;

  private String apiVersion = "0.6";

  private String prefix;

  private String username;
  private Long uid;
  private String displayName;


  private OsmObjectVisitor<String> getApiType = new OsmObjectVisitor<String>() {
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

  public void close() throws Exception {
    httpClient.close();
  }

  private static Pattern displayNamePattern = Pattern.compile("<user\\s*.* display_name=\"([^\"]+)\"");
  private static Pattern uidPattern = Pattern.compile("<user\\s*.* id=\"([^\"]+)\"");

  public void authenticate(String username, String password) throws Exception {

    httpClient.close();

    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(username, password));


    httpClient = HttpClientBuilder.create()
        .setDefaultCredentialsProvider(credsProvider)
        .setUserAgent("osm-common")
        .build();

    this.username = username;


    CloseableHttpResponse response = httpClient.execute(new HttpGet(prefix + "user/details"));
    try {

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("HTTP status " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
      }

      String xml = IOUtils.toString(response.getEntity().getContent(), "UTF8");
      xml = xml.replaceAll("\n+", " ");

      Matcher matcher = uidPattern.matcher(xml);
      if (!matcher.find()) {
        throw new RuntimeException("No user id in xml!\n" + xml);
      }
      this.uid = Long.valueOf(matcher.group(1));

      matcher = displayNamePattern.matcher(xml);
      if (!matcher.find()) {
        throw new RuntimeException("No display name in xml!\n" + xml);
      }
      this.displayName = matcher.group(1);


    } finally {
      response.close();
    }


  }

  public Root get(double south, double west, double north, double east) throws Exception {
    Root root = new PojoRoot();
    InstantiatedOsmXmlParserDelta delta = get(root, south, west, north, east);
    return root;
  }

  /**
   * HTTP status code 400 (Bad Request)
   * When any of the node/way/relation limits are crossed
   * <p/>
   * HTTP status code 509 (Bandwidth Limit Exceeded)
   * "Error: You have downloaded too much data. Please try again later." See Developer FAQ.
   *
   * @param root
   * @param south
   * @param west
   * @param north
   * @param east
   * @throws Exception
   */
  public InstantiatedOsmXmlParserDelta get(Root root, double south, double west, double north, double east) throws Exception {

    DecimalFormat df = new DecimalFormat("#.#######");

    String bottom = df.format(south);
    String left = df.format(west);
    String top = df.format(north);
    String right = df.format(east);

    CloseableHttpResponse response = httpClient.execute(new HttpGet(prefix + "map?bbox=" + left + "," + bottom + "," + right + "," + top));
    try {

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("HTTP status " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
      }

      InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
      parser.setRoot(root);
      return parser.parse(response.getEntity().getContent());

    } finally {
      response.close();
    }

  }

  /**
   * @param comment
   * @return id of new changeset
   */
  public long createChangeset(String comment) throws Exception {

    StringWriter xml = new StringWriter(1024);

    String generator = getClass().getName() + "#createChangeset";

    xml.write("<?xml version='1.0' encoding='UTF-8'?>\n");
    xml.write("<osm version='");
    xml.write(apiVersion);
    xml.write("' generator='");
    xml.write(generator);
    xml.write("'>\n");

    xml.write("<changeset>");
    xml.write("<tag k='created_by' v='");
    xml.write(generator);
    xml.write("'/>");

    if (comment != null) {
      xml.write("<tag k='comment' v='");
      xml.write(comment);
      xml.write("'/>");
    }

    xml.write("</changeset>");

    xml.write("</osm>\n");


    HttpPut put = new HttpPut(prefix + "changeset/create");
    put.setEntity(new StringEntity(xml.toString(), "UTF8"));

    CloseableHttpResponse response = httpClient.execute(put);
    try {

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("HTTP status " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
      }

      long id = Long.valueOf(IOUtils.toString(response.getEntity().getContent()));
      return id;

    } finally {
      response.close();
    }

    /*

Create: PUT /api/0.6/changeset/create[edit]
The payload of a changeset creation request has to be one or more changeset elements optionally including an arbitrary number of tags.

    <osm>
  <changeset>
    <tag k="created_by" v="JOSM 1.61"/>
    <tag k="comment" v="Just adding some streetnames"/>
    ...
  </changeset>
  ...
</osm>

The ID of the newly created changeset with a content type of text/plain


     */


  }

  public void closeChangeset(long id) throws Exception {

    /*

    Close: PUT /api/0.6/changeset/#id/close[edit]
Closes a changeset. A changeset may already have been closed without the owner issuing this API call. In this case an error code is returned.
Parameters[edit]
id
The id of the changeset to close. The user issuing this API call has to be the same that created the changeset.


     */

    HttpPut put = new HttpPut(prefix + "changeset/" + id + "/close");

    CloseableHttpResponse response = httpClient.execute(put);
    try {

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("HTTP status " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
      }

    } finally {
      response.close();
    }

  }

  public Node getNode(long id) throws Exception {
    Root root = new PojoRoot();
    InstantiatedOsmXmlParserDelta delta = getNode(root, id);
    return delta.getCreatedNodes().iterator().next();
  }

  public InstantiatedOsmXmlParserDelta getNode(Root root, long id) throws Exception {
    HttpGet get = new HttpGet(prefix + "node/" + id);
    CloseableHttpResponse response = httpClient.execute(get);
    try {

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("HTTP status " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
      }

      InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
      parser.setRoot(root);
      return parser.parse(new InputStreamReader(response.getEntity().getContent(), "UTF8"));

    } finally {
      response.close();
    }

  }

  public Way getWay(long id) throws Exception {
    Root root = new PojoRoot();
    InstantiatedOsmXmlParserDelta delta = getWay(root, id);
    return delta.getCreatedWays().iterator().next();
  }

  public InstantiatedOsmXmlParserDelta getWay(Root root, long id) throws Exception {
    HttpGet get = new HttpGet(prefix + "way/" + id);
    CloseableHttpResponse response = httpClient.execute(get);
    try {

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("HTTP status " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
      }

      InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
      parser.setRoot(root);
      return parser.parse(new InputStreamReader(response.getEntity().getContent(), "UTF8"));

    } finally {
      response.close();
    }

  }

  public Relation getRelation(long id) throws Exception {
    Root root = new PojoRoot();
    InstantiatedOsmXmlParserDelta delta = getRelation(root, id);
    return delta.getCreatedRelations().iterator().next();
  }

  public InstantiatedOsmXmlParserDelta getRelation(Root root, long id) throws Exception {
    HttpGet get = new HttpGet(prefix + "relation/" + id);
    CloseableHttpResponse response = httpClient.execute(get);
    try {

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("HTTP status " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
      }

      InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
      parser.setRoot(root);
      return parser.parse(new InputStreamReader(response.getEntity().getContent(), "UTF8"));

    } finally {
      response.close();
    }

  }


  public void create(long changeset, OsmObject object) throws Exception {

    object.setTimestamp(LocalDateTime.now(Clock.systemUTC()));
    object.setChangeset(changeset);

    StringWriter sw = new StringWriter(4096);
    OsmXmlWriter xml = new OsmXmlWriter(sw);
    xml.write(object);
    xml.close();


    HttpPut put = new HttpPut(prefix + object.accept(getApiType) + "/create");

    put.setEntity(new StringEntity(sw.toString(), "UTF8"));

    CloseableHttpResponse response = httpClient.execute(put);
    try {

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("HTTP status " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
      }

      long id = Long.valueOf(IOUtils.toString(response.getEntity().getContent()));
      object.setId(id);
      object.setVersion(1);
      object.setVisible(true);
      object.setUid(uid);
      object.setUser(displayName);


    } finally {
      response.close();
    }

    object.setChangeset(changeset);


  }

  public void update(OsmObject object) {
    throw new UnsupportedOperationException();
  }

  public void delete(long changeset, OsmObject object) throws Exception {


    StringWriter xml = new StringWriter(1024);

    String generator = getClass().getName() + "#createChangeset";

    xml.write("<?xml version='1.0' encoding='UTF-8'?>\n");
    xml.write("<osm version='");
    xml.write(apiVersion);
    xml.write("' generator='");
    xml.write(generator);
    xml.write("'>\n");

    xml.write("<");
    xml.write(object.accept(getApiType));
    xml.write(" id='");
    xml.write(String.valueOf(object.getId()));
    xml.write("' version='");
    xml.write(String.valueOf(object.getVersion()));
    xml.write("' changeset='");
    xml.write(String.valueOf(changeset));
    if (object instanceof Node) {
      // todo really need this?
      Node node = (Node) object;
      xml.write("' lat='");
      xml.write(String.valueOf(node.getLatitude()));
      xml.write("' lon='");
      xml.write(String.valueOf(node.getLongitude()));
    }
    xml.write("' />");

    xml.write("</osm>\n");

    HttpPut put = new HttpPut(prefix + object.accept(getApiType) + "/" + object.getId());
    put.setHeader("X_HTTP_METHOD_OVERRIDE", "DELETE");

    put.setEntity(new StringEntity(xml.toString(), "UTF8"));

    CloseableHttpResponse response = httpClient.execute(put);
    try {

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("HTTP status " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
      }

      int version = Integer.valueOf(IOUtils.toString(response.getEntity().getContent()));
      object.setVersion(version);
      object.setVisible(false);
      object.setUid(uid);
      object.setUser(displayName);


    } finally {
      response.close();
    }

    object.setChangeset(changeset);


  }


}
