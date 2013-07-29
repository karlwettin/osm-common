package se.kodapan.osm.geo.distance;

import se.kodapan.osm.domain.*;

/**
 * @author kalle
 * @since 2013-07-27 20:49
 */
public abstract class Distance {

  /**
   *
   * @param latitudeA
   * @param longitudeA
   * @param latitudeB
   * @param longitudeB
   */
  public abstract double calculate(double latitudeA, double longitudeA, double latitudeB, double longitudeB);

}
