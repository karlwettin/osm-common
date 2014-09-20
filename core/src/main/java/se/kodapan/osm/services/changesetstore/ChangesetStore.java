package se.kodapan.osm.services.changesetstore;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.osm.services.HttpService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Parses a file system structure as described by
 * http://wiki.openstreetmap.org/wiki/Planet.osm/diffs
 *
 * @author kalle
 * @since 2013-05-03 20:07
 */
public class ChangesetStore extends HttpService {

  private static Logger log = LoggerFactory.getLogger(ChangesetStore.class);

  private URL baseURL;

  /**
   * @param baseURL
   * @throws MalformedURLException
   */
  public void setBaseURL(String baseURL) throws MalformedURLException {
    setBaseURL(new URL(baseURL));
  }

  public void setBaseURL(URL baseURL) {
    if (baseURL.getQuery() != null) {
      throw new IllegalArgumentException("No query allowed in base URL.");
    }
    if (baseURL.getRef() != null) {
      throw new IllegalArgumentException("No reference allowed in base URL.");
    }
    if (!baseURL.getPath().endsWith("/")) {
      try {
        baseURL = new URL(baseURL.toString() + "/");
      } catch (MalformedURLException e) {
        throw new RuntimeException("Bad code! Attempting to add a / as suffix to the URL " + baseURL.toString(), e);
      }
    }
    this.baseURL = baseURL;
  }

  /**
   * DO NOT FORGET TO CLOSE THE READER!
   *
   * @param sequenceNumber
   * @return
   * @throws Exception
   */
  public Reader getChangeset(int sequenceNumber) throws Exception {

    URL url = getChangesetURL(sequenceNumber);

    log.info("Downloading " + url);

    HttpGet get = new HttpGet(url.toString());
    HttpResponse httpResponse = getHttpClient().execute(get);
    setUserAgent(get);

    if (httpResponse.getStatusLine().getStatusCode() != 200) {
      log.info("HTTP " + httpResponse.getStatusLine().getStatusCode() + " for " + url.toString());
      return null;
    }

    InputStream inputStream = httpResponse.getEntity().getContent();

    return new InputStreamReader(new GZIPInputStream(inputStream), "UTF8");

  }


  public ChangesetStoreState getMostRecentChangesetState() throws Exception {
    ChangesetStoreState placeholder = parseChangesetState(new URL(baseURL.toString() + "state.txt"));
    return getChangesetState(placeholder.getSequenceNumber());
  }

  public ChangesetStoreState getChangesetState(int sequenceNumber) throws Exception {
    return parseChangesetState(getChangesetStateURL(sequenceNumber));
  }

  private static final int maximumSequenceNumber = 999999999;

  public URL getChangesetURL(int sequenceNumber) {

    if (sequenceNumber < 0) {
      throw new IllegalArgumentException("Sequence numbers must not be negative.");
    } else if (sequenceNumber > maximumSequenceNumber) {
      throw new IllegalArgumentException("Sequence numbers must not be larger than " + maximumSequenceNumber);
    }

    StringBuilder url = new StringBuilder();
    url.append(String.valueOf(sequenceNumber));
    while (url.length() < 9) {
      url.insert(0, "0");
    }
    url.insert(3, "/");
    url.insert(7, "/");
    url.append(".osc.gz");

    url.insert(0, baseURL.toString());

    try {
      return new URL(url.toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException("Bad code! See #setBaseURL or #getSequenceURL", e);
    }

  }

  public URL getChangesetStateURL(int sequenceNumber) {

    if (sequenceNumber < 0) {
      throw new IllegalArgumentException("Sequence numbers must not be negative.");
    } else if (sequenceNumber > maximumSequenceNumber) {
      throw new IllegalArgumentException("Sequence numbers must not be larger than " + maximumSequenceNumber);
    }

    StringBuilder url = new StringBuilder();
    url.append(String.valueOf(sequenceNumber));
    while (url.length() < 9) {
      url.insert(0, "0");
    }
    url.insert(3, "/");
    url.insert(7, "/");
    url.append(".state.txt");

    url.insert(0, baseURL.toString());

    try {
      return new URL(url.toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException("Bad code! See #setBaseURL or #getSequenceURL", e);
    }

  }

  private static Pattern contentTypeEncodingPattern = Pattern.compile(".+charset\\s*=\\s*(.+)\\s*$", Pattern.CASE_INSENSITIVE);

  public ChangesetStoreState parseChangesetState(URL url) throws Exception {

    HttpGet get = new HttpGet(url.toString());
    setUserAgent(get);
    HttpResponse httpResponse = getHttpClient().execute(get);


    if (httpResponse.getStatusLine().getStatusCode() != 200) {
      log.info("HTTP " + httpResponse.getStatusLine().getStatusCode() + " for " + url.toString());
      return null;
    }

    InputStream inputStream = httpResponse.getEntity().getContent();

    String contentEncoding = null;
    if (httpResponse.getEntity().getContentEncoding() != null) {
      contentEncoding = httpResponse.getEntity().getContentEncoding().getValue();
    } else if (httpResponse.getEntity().getContentType() != null) {
      Matcher matcher = contentTypeEncodingPattern.matcher(httpResponse.getEntity().getContentType().getValue());
      if (matcher.find()) {
        contentEncoding = matcher.group(1);
      }
    }


    try {
      if (contentEncoding == null) {
        return parseChangesetState(new InputStreamReader(inputStream));
      } else {
        return parseChangesetState(new InputStreamReader(inputStream, contentEncoding));
      }
    } finally {
      inputStream.close();
    }

  }

  private static final Pattern commentPattern = Pattern.compile("^\\s*#.*$");
  private static final Pattern propertyPattern = Pattern.compile("^([^=]+)=(.+)$");

  public ChangesetStoreState parseChangesetState(Reader reader) throws Exception {
    ChangesetStoreState state = new ChangesetStoreState();


    BufferedReader bufferedReader = new BufferedReader(reader);
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      if (!commentPattern.matcher(line).matches()) {
        Matcher matcher = propertyPattern.matcher(line);
        if (!matcher.matches()) {
          log.warn("Ignoring invalid formatted state file property line " + line);
        } else {
          String key = matcher.group(1).trim();
          String value = matcher.group(2).trim();

          if ("sequenceNumber".equalsIgnoreCase(key)) {
            state.setSequenceNumber(Integer.valueOf(value));
          } else if ("timestamp".equalsIgnoreCase(key)) {
            state.setTimestamp(new ChangesetStateTimestampFormat().parse(value).getTime());
          } else if ("txnMaxQueried".equalsIgnoreCase(key)) {
            if (!value.trim().isEmpty()) {
              state.setTxnMaxQueried(Long.valueOf(value));
            }
          } else if ("txnMax".equalsIgnoreCase(key)) {
            if (!value.trim().isEmpty()) {
              state.setTxnMax(Long.valueOf(value));
            }
          } else if ("txnActiveList".equalsIgnoreCase(key)) {
            if (!value.trim().isEmpty()) {
              String[] list = value.split(",");
              state.setTxnActiveList(new ArrayList<Long>(list.length));
              for (String txn : list) {
                state.getTxnActiveList().add(Long.valueOf(txn));
              }
            }
          } else if ("txnReadyList".equalsIgnoreCase(key)) {
            if (!value.trim().isEmpty()) {
              String[] list = value.split(",");
              state.setTxnReadyList(new ArrayList<Long>(list.length));
              for (String txn : list) {
                state.getTxnReadyList().add(Long.valueOf(txn));
              }
            }
          } else {
            log.warn("Skipping unknown state file property " + line);
          }
        }
      }
    }


    return state;
  }


  public URL getBaseURL() {
    return baseURL;
  }

  public ChangesetStoreState findFirstChangesetStateSince(long time) throws Exception {
    ChangesetStoreState state = getMostRecentChangesetState();
    while (state.getSequenceNumber() > 0 && time < state.getTimestamp()) {
      ChangesetStoreState previousState = getChangesetState(state.getSequenceNumber() - 1);
      if (previousState.getTimestamp() == time) {
        return state;
      }
      state = previousState;
    }
    return state;
  }

  public List<ChangesetStoreState> findChangesetStatesSince(long since) throws Exception {
    List<ChangesetStoreState> states = new ArrayList<ChangesetStoreState>(100);

    ChangesetStoreState state = getMostRecentChangesetState();
    while (state.getSequenceNumber() > 0 && since < state.getTimestamp()) {
      states.add(0, state);
      ChangesetStoreState previousState = getChangesetState(state.getSequenceNumber() - 1);
      // todo this will fail if one state file in the middle of them all is missing on the server
      if (previousState == null || previousState.getTimestamp() == since) {
        return states;
      }
      state = previousState;
    }
    if (since < state.getTimestamp()) {
      states.add(0, state);
    }
    return states;
  }

}
