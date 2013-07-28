package se.kodapan.osm.services.overpass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kalle
 * @since 2013-07-27 23:53
 */
public abstract class AbstractCachedOverpass extends Overpass {

  private static Logger log = LoggerFactory.getLogger(AbstractCachedOverpass.class);

  @Override
  public String execute(String overpassQuery, String queryDescription) throws Exception {
    String response = getCachedResponse(overpassQuery);
    if (response == null) {
      log.info("No cached response available, sending query to super.");
      response = super.execute(overpassQuery, queryDescription);
      setCachedResponse(overpassQuery, response);
    } else if (log.isDebugEnabled()) {
      log.debug("Cached response available.");
    }

    return response;
  }

  public abstract String getCachedResponse(String url) throws Exception;

  public abstract void setCachedResponse(String url, String response) throws Exception;


}
