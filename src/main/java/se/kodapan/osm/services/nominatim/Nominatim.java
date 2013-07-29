package se.kodapan.osm.services.nominatim;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStreamReader;
import java.io.Reader;

/**
 * http://wiki.openstreetmap.org/wiki/Nominatim
 * http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy
 *
 * @author kalle
 * @since 2013-03-26 20:35
 */
public class Nominatim {

  /** setter method enforce < 1000. See http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy */
  private long minimumMillisecondsDelayBetweenRequests = 5000;

  private long previousRequestTimestamp = 0;
  private static String defaultUserAgent = "Unnamed instance of " + Nominatim.class.getName() + ", https://github.com/karlwettin/osm-common/";
  private String userAgent = defaultUserAgent;
  private HttpClient httpClient = new DefaultHttpClient();

  public void open() throws Exception {

  }

  public void close() throws Exception {

  }

  public synchronized String search(String url) throws Exception {

    if (defaultUserAgent.equals(userAgent)) {
      throw new NullPointerException("Nominatim HTTP header User-Agent not set!");
    }

    try {
      long sleep = previousRequestTimestamp + minimumMillisecondsDelayBetweenRequests - System.currentTimeMillis();
      if (sleep > 0) {
        Thread.sleep(sleep);
      }

      HttpGet get = new HttpGet(url.toString());
      get.setHeader("User-Agent", userAgent);
      get.setHeader("Content-Encoding", "application/x-www-form-encoded");
      HttpResponse response = httpClient.execute(get);

      Reader reader = new InputStreamReader(response.getEntity().getContent(), "UTF8");
      String string;
      try {
        string = IOUtils.toString(reader);
      } catch (Exception e) {
        throw e;
      } finally {
        reader.close();
      }

      return string;

    } finally {
      previousRequestTimestamp = System.currentTimeMillis();
    }
  }

  public long getMinimumMillisecondsDelayBetweenRequests() {
    return minimumMillisecondsDelayBetweenRequests;
  }

  /**
   * throws IllegalArgumentException if parameter is less than one second.
   * http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy
   *
   * @param minimumMillisecondsDelayBetweenRequests
   */
  public void setMinimumMillisecondsDelayBetweenRequests(long minimumMillisecondsDelayBetweenRequests) {
    if (minimumMillisecondsDelayBetweenRequests < 1000) {
      throw new IllegalArgumentException("Must be at least 1 second delay! See http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy");
    }
    this.minimumMillisecondsDelayBetweenRequests = minimumMillisecondsDelayBetweenRequests;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }
}
