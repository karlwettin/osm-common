package se.kodapan.osm.util.distance;

/**
 * @author kalle
 * @since 2013-10-24 2:47 PM
 */
public class TestManhattanDistance extends DistanceMetricsTest {

  @Override
  public Distance distanceMetricsFactory() {
    return new ManhattanDistance();
  }
}
