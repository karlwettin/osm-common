package se.kodapan.osm.services.overpass;

/**
 * @author kalle
 * @since 2013-09-09 3:50 PM
 */
public class OverpassException extends Exception {

  public OverpassException() {
  }

  public OverpassException(String message) {
    super(message);
  }

  public OverpassException(String message, Throwable cause) {
    super(message, cause);
  }

  public OverpassException(Throwable cause) {
    super(cause);
  }

}
