package se.kodapan.osm.services;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kalle
 * @since 2013-09-21 6:48 PM
 */
public class HttpService {

  private static Logger log = LoggerFactory.getLogger(HttpService.class);

  private long minimumMillisecondsDelayBetweenRequests = 0;
  private long previousRequestTimestamp = 0;

  private ClientConnectionManager cm;
  private HttpClient httpClient;

  private String defaultUserAgent = "Unnamed instance of " + getClass().getName() + ", https://github.com/karlwettin/osm-common/";
  private String userAgent = defaultUserAgent;

  public void open() throws Exception {
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

    cm = new ThreadSafeClientConnManager(new BasicHttpParams(), schemeRegistry);
    httpClient = new DefaultHttpClient(cm, new BasicHttpParams());
  }

  public void setUserAgent(HttpRequest httpRequest) {
    if (defaultUserAgent.equals(userAgent)) {
      throw new NullPointerException("HTTP header User-Agent not set! See se.kodapan.osm.services.HttpService#setUserAgent");
    }
    httpRequest.setHeader("User-Agent", userAgent);
  }

  public void close() throws Exception {

  }

  public void leniency() throws Exception {
    long sleep;
    while ((sleep = previousRequestTimestamp + minimumMillisecondsDelayBetweenRequests - System.currentTimeMillis()) > 0) {
      Thread.sleep(sleep);
    }
    previousRequestTimestamp = System.currentTimeMillis();
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public long getMinimumMillisecondsDelayBetweenRequests() {
    return minimumMillisecondsDelayBetweenRequests;
  }

  public void setMinimumMillisecondsDelayBetweenRequests(long minimumMillisecondsDelayBetweenRequests) {
    this.minimumMillisecondsDelayBetweenRequests = minimumMillisecondsDelayBetweenRequests;
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }

  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }
}
