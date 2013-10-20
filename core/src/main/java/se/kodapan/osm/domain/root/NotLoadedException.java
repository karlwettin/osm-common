package se.kodapan.osm.domain.root;

import se.kodapan.osm.domain.OsmObject;

/**
 * Created by kalle on 10/20/13.
 */
public class NotLoadedException extends RuntimeException {

  public NotLoadedException() {
  }

  public NotLoadedException(OsmObject object) {
    this(object.getClass().getSimpleName() + "#id " + object.getId() + " is not loaded!");
  }

  public NotLoadedException(String detailMessage) {
    super(detailMessage);
  }

  public NotLoadedException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }

  public NotLoadedException(Throwable throwable) {
    super(throwable);
  }
}
