package se.kodapan.osm.services.nominatim;

/**
 * @author kalle
 * @since 2013-07-28 19:57
 */
public abstract class AbstractCachedNominatim extends Nominatim {

  @Override
  public synchronized String search(String url) throws Exception {
    String response = getCachedResponse(url);
    if (response == null) {
      response = super.search(url);
      setCachedResponse(url, response);
    }
    return response;

  }

  public abstract String getCachedResponse(String url) throws Exception;

  public abstract void setCachedResponse(String url, String response) throws Exception;

}
