package se.kodapan.osm.services.overpass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kalle
 * @since 2013-07-27 23:53
 */
public abstract class AbstractCachedOverpass extends Overpass {

  protected Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public String execute(String overpassQuery, String queryDescription) throws OverpassException {
    try {
      String response = getCachedResponse(overpassQuery);
      if (response == null) {
        log.info("No cached response available, sending query to super.");
        response = super.execute(overpassQuery, queryDescription);
        setCachedResponse(overpassQuery, response);
      } else if (log.isDebugEnabled()) {
        log.debug("Cached response available.");
      }
      return response;
    } catch (Exception e) {
      throw new OverpassException(e);
    }
  }

  public abstract String getCachedResponse(String url) throws Exception;

  public abstract void setCachedResponse(String url, String response) throws Exception;


}
