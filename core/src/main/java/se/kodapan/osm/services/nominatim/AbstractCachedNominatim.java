package se.kodapan.osm.services.nominatim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kalle
 * @since 2013-07-28 19:57
 */
public abstract class AbstractCachedNominatim extends Nominatim {

  protected Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public synchronized String search(String url) throws Exception {
    String response = getCachedResponse(url);
    if (response == null) {
      log.info("No cached response available, sending query to super.");
      response = super.search(url);
      setCachedResponse(url, response);
    } else if (log.isDebugEnabled()) {
      log.debug("Cached response available.");
    }
    return response;

  }

  public abstract String getCachedResponse(String url) throws Exception;

  public abstract void setCachedResponse(String url, String response) throws Exception;

}
