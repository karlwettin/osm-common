package se.kodapan.osm.services.overpass;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.osm.services.HttpService;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2012-12-31 16:32
 */
public class Overpass extends HttpService {

  private static Logger log = LoggerFactory.getLogger(Overpass.class);

  private String serverURL = "http://www.overpass-api.de/api/interpreter";

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

      HttpPost post = new HttpPost(serverURL);
      setUserAgent(post);

      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
      nameValuePairs.add(new BasicNameValuePair("data", overpassQuery));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));


      leniency();

      log.debug("Executing overpass query: " + queryDescription + "\n" + overpassQuery);

      long started = System.currentTimeMillis();
      HttpResponse response = getHttpClient().execute(post);

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


}
