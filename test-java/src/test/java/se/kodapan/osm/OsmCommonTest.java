package se.kodapan.osm;

import junit.framework.TestCase;
import se.kodapan.osm.services.HttpService;

/**
 * @author kalle
 * @since 2013-07-29 19:13
 */
public abstract class OsmCommonTest extends TestCase {

  public void setUserAgent(HttpService service) {
    service.setUserAgent("test suite of <https://github.com/karlwettin/osm-common/>");
  }


}
