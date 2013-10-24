package se.kodapan.osm.util.distance;

import junit.framework.TestCase;

/**
 * @author kalle
 * @since 2013-10-24 2:50 PM
 */
public abstract class DistanceMetricsTest extends TestCase {

  public abstract Distance distanceMetricsFactory();

  public void test() {

    Distance metrics = distanceMetricsFactory();

    double distanceMalmoStockholm = metrics.calculate(55.58278064008449, 13.009630050000055, 59.326294118560725, 17.98754555000005);
    double distanceStockholmMalmo = metrics.calculate(59.326294118560725, 17.98754555000005, 55.58278064008449, 13.009630050000055);

    assertEquals(distanceMalmoStockholm, distanceStockholmMalmo);

  }

}
