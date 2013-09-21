package se.kodapan.osm;

import junit.framework.TestCase;
import se.kodapan.osm.services.nominatim.Nominatim;
import se.kodapan.osm.services.overpass.Overpass;

/**
 * @author kalle
 * @since 2013-07-29 19:13
 */
public abstract class OsmCommonTest extends TestCase {

  public void setUserAgent(Nominatim nominatim) {
    nominatim.setUserAgent("test suite of <https://github.com/karlwettin/osm-common/>");
  }

  public void setUserAgent(Overpass overpass) {
    overpass.setUserAgent("test suite of <https://github.com/karlwettin/osm-common/>");
  }

}
