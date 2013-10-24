package se.kodapan.osm.util.distance;

/**
 * @author kalle
 * @since 2013-10-24 2:45 PM
 */
public class ManhattanDistance extends Distance {

  @Override
  public double calculate(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
    return Math.abs(latitudeB - latitudeA) + Math.abs(longitudeB - longitudeA);
  }
}
