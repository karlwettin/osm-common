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
 *
 * @author kalle
 * @since 2013-03-26 20:35
 */
public class Nominatim {

  private long previousRequestTimestamp = 0;

  private long minimumMillisecondsDelayBetweenRequests = 1000;

  private HttpClient httpClient = new DefaultHttpClient();

  public void open() throws Exception {

  }

  public void close() throws Exception {

  }

  public synchronized String search(String url) throws Exception {

    try {
      long sleep = previousRequestTimestamp + minimumMillisecondsDelayBetweenRequests - System.currentTimeMillis();
      if (sleep > 0) {
        Thread.sleep(sleep);
      }

      HttpGet get = new HttpGet(url.toString());
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

  public void setMinimumMillisecondsDelayBetweenRequests(long minimumMillisecondsDelayBetweenRequests) {
    this.minimumMillisecondsDelayBetweenRequests = minimumMillisecondsDelayBetweenRequests;
  }
}
