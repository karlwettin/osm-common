package se.kodapan.osm.services.nominatim;

import se.kodapan.osm.OsmCommonTest;

import java.io.File;

/**
 * @author kalle
 * @since 2013-07-28 20:00
 */
public class TestFileSystemCachedNominatim extends OsmCommonTest {

  int getCachedIndex = 0;
  int setCachedIndex = 0;

  public void test() throws Exception {

    AbstractCachedNominatim nominatim = new FileSystemCachedNominatim(new File("target/TestFileSystemCachedNominatim/" + System.currentTimeMillis())) {

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

    setUserAgent(nominatim);

    nominatim.open();

    assertEquals(0, getCachedIndex);
    assertEquals(0, setCachedIndex);

    nominatim.search(new NominatimQueryBuilder().setQuery("halmstad, sweden").build());

    assertEquals(1, getCachedIndex);
    assertEquals(1, setCachedIndex);

    nominatim.search(new NominatimQueryBuilder().setQuery("halmstad, sweden").build());

    assertEquals(2, getCachedIndex);
    assertEquals(1, setCachedIndex);

    nominatim.close();

  }

}
