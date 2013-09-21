package se.kodapan.osm.slippymap;

import junit.framework.TestCase;

import java.util.Random;

/**
 * @author kalle
 * @since 2013-09-21 4:39 PM
 */
public abstract class SlippyMapTest extends TestCase {

  protected abstract SlippyMap mapFactory();

  public void test() throws Exception {

    SlippyMap slippyMap = mapFactory();

    testTile(slippyMap, 10, 10, 10);

    Random random = new Random();
    for (int i = 0; i < 100; i++) {
      double latitude = (random.nextDouble() * 180) - 90;
      double longitude = (random.nextDouble() * 360) - 180;

      int zoom = random.nextInt(20);

      testTile(slippyMap, latitude, longitude, zoom);

    }

  }

  private void testTile(SlippyMap slippyMap, double latitude, double longitude, int zoom) {
    Tile tile = slippyMap.tileFactory(longitude, latitude, zoom);
    assertTrue("Latitude supposed to be " + tile.getSouthLatitude() + " >= " + latitude + " <= " + tile.getNorthLatitude(),
        latitude >= tile.getSouthLatitude() && latitude <= tile.getNorthLatitude());
    assertTrue("Longitude supposed to be " + tile.getWestLongitude() + " >= " + longitude + " <= " + tile.getEastLongitude(),
        longitude >= tile.getWestLongitude() && longitude <= tile.getEastLongitude());
  }

}
