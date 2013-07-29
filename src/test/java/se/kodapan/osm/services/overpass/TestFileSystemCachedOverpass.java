package se.kodapan.osm.services.overpass;

import junit.framework.TestCase;
import se.kodapan.osm.OsmCommonTest;
import se.kodapan.osm.domain.Node;

import java.io.File;

/**
 * @author kalle
 * @since 2013-07-28 00:24
 */
public class TestFileSystemCachedOverpass extends OsmCommonTest {

  int getCachedIndex = 0;
  int setCachedIndex = 0;

  public void test() throws Exception {

    AbstractCachedOverpass decoration = new FileSystemCachedOverpass(new File("target/TestFileSystemCachedOverpass/" + System.currentTimeMillis())) {

      String cached = null;

      @Override
      public String getCachedResponse(String url) throws Exception {
        String cachedResponse = super.getCachedResponse(url);
        if (getCachedIndex == 0) {
          assertNull(cachedResponse);
        } else if (getCachedIndex == 1) {
          assertNotNull(cachedResponse);
          assertEquals(cached, cachedResponse);
        }
        getCachedIndex++;
        return cachedResponse;
      }

      @Override
      public void setCachedResponse(String url, String response) throws Exception {
        cached = response;
        super.setCachedResponse(url, response);
        if (setCachedIndex == 1) {
          fail();
        }
        setCachedIndex++;

      }
    };

    Overpass overpass = decoration;
    setUserAgent(overpass);

    overpass.open();

    assertEquals(0, getCachedIndex);
    assertEquals(0, setCachedIndex);

    Node first = overpass.getNode(561366406l);

    assertEquals(1, getCachedIndex);
    assertEquals(1, setCachedIndex);

    Node second = overpass.getNode(561366406l);
    assertEquals(2, getCachedIndex);
    assertEquals(1, setCachedIndex);

    overpass.close();

  }

}
