package se.kodapan.osm.services.overpass;

/**
 * @author kalle
 * @since 2013-07-27 23:53
 */
public abstract class AbstractCachedOverpass extends Overpass {

  @Override
  public String execute(String overpassQuery, String queryDescription) throws Exception {
    String response = getCachedResponse(overpassQuery);
    if (response == null) {
      response = super.execute(overpassQuery, queryDescription);
      setCachedResponse(overpassQuery, response);
    }
    return response;
  }

  public abstract String getCachedResponse(String url) throws Exception;

  public abstract void setCachedResponse(String url, String response) throws Exception;


}
