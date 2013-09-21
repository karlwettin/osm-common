package se.kodapan.osm.geo.distance;

/**
 * Euclidean squared distance metrics.
 *
 * @author kalle
 * @since 2013-07-29 15:19
 */
public class EuclideanDistance extends Distance {

  @Override
  public double calculate(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {

    double latDistance = latitudeB - latitudeA;
    double lngDistance = longitudeB - longitudeA;
    double result = 0;
    result += latDistance * latDistance;
    result += lngDistance * lngDistance;
    return Math.sqrt(result);

  }

}
