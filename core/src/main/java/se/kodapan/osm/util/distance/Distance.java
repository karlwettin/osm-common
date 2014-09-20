package se.kodapan.osm.util.distance;

import se.kodapan.osm.domain.Node;

/**
 * @author kalle
 * @since 2013-07-27 20:49
 */
public abstract class Distance {

  public double calculate(Node a, Node b) {
    return calculate(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
  }

  /**
   * @param latitudeA
   * @param longitudeA
   * @param latitudeB
   * @param longitudeB
   */
  public abstract double calculate(double latitudeA, double longitudeA, double latitudeB, double longitudeB);


}
