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

  /**
   * See http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy
   */
  private long minimumMillisecondsDelayBetweenRequests = 1000;

  private long previousRequestTimestamp = 0;
  private static String defaultUserAgent = "Unnamed instance of " + Nominatim.class.getName() + ", https://github.com/karlwettin/osm-common/";
  private String userAgent = defaultUserAgent;
  private HttpClient httpClient = new DefaultHttpClient();

  public void open() throws Exception {

  }

  public void close() throws Exception {

  }

  public String search(String url) throws Exception {

    if (defaultUserAgent.equals(userAgent)) {
      throw new NullPointerException("Nominatim HTTP header User-Agent not set!");
    }

    long sleep;
    while ((sleep = previousRequestTimestamp + minimumMillisecondsDelayBetweenRequests - System.currentTimeMillis()) > 0) {
      Thread.sleep(sleep);
    }
    previousRequestTimestamp = System.currentTimeMillis();

    HttpGet get = new HttpGet(url);
    get.setHeader("User-Agent", userAgent);
    get.setHeader("Content-Encoding", "application/x-www-form-encoded");
    HttpResponse response = httpClient.execute(get);

    String string;
    Reader reader = new InputStreamReader(response.getEntity().getContent(), "UTF8");
    try {
      string = IOUtils.toString(reader);
    } finally {
      reader.close();
    }

    return string;

  }

  public long getMinimumMillisecondsDelayBetweenRequests() {
    return minimumMillisecondsDelayBetweenRequests;
  }

  /**
   * Public servers require 1000 milliseconds.
   * http://wiki.openstreetmap.org/wiki/Nominatim_usage_policy
   *
   * @param minimumMillisecondsDelayBetweenRequests
   *
   */
  public void setMinimumMillisecondsDelayBetweenRequests(long minimumMillisecondsDelayBetweenRequests) {
    this.minimumMillisecondsDelayBetweenRequests = minimumMillisecondsDelayBetweenRequests;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }
}
