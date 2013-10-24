package se.kodapan.osm.util.distance;

/**
 * @author kalle
 * @since 2013-10-24 2:52 PM
 */
public class TestArcDistance extends DistanceMetricsTest {

  @Override
  public Distance distanceMetricsFactory() {
    return new ArcDistance();
  }

}
