package se.kodapan.osm.domain;

/**
 * @author kalle
 * @since 2013-07-27 20:49
 */
public class Distance {


  public static double euclideanDistance(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {

    double latDistance = latitudeB - latitudeA;
    double lngDistance = longitudeB - longitudeA;
    double result = 0;
    result += latDistance * latDistance;
    result += lngDistance * lngDistance;
    return Math.sqrt(result);

  }


  /**
   * copied from org.apache.lucene.spatial.base.LatLng
   *
   * @param latitudeA
   * @param longitudeA
   * @param latitudeB
   * @param longitudeB
   * @return kilometers
   */
  public static double arcDistance(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {

    longitudeA = normalizeLongitude(longitudeA);
    longitudeB = normalizeLongitude(longitudeB);

    // Check for same position
    if (latitudeA == latitudeB && longitudeA == longitudeB)
      return 0.0;

    // Get the m_dLongitude diffeernce. Don't need to worry about
    // crossing 180 since cos(x) = cos(-x)
    double dLon = longitudeB - longitudeA;

    double a = radians(90.0 - latitudeA);
    double c = radians(90.0 - latitudeB);
    double cosB = (Math.cos(a) * Math.cos(c))
        + (Math.sin(a) * Math.sin(c) * Math.cos(radians(dLon)));

    //    double radius = (lUnits == DistanceUnits.MILES) ? 3963.205/* MILERADIUSOFEARTH */
    //        : 6378.160187/* KMRADIUSOFEARTH */;

    double radius = 6378.160187;

    // Find angle subtended (with some bounds checking) in radians and
    // multiply by earth radius to find the arc distance
    if (cosB < -1.0)
      return 3.14159265358979323846/* PI */ * radius;
    else if (cosB >= 1.0)
      return 0;
    else
      return Math.acos(cosB) * radius;
  }

  /**
   * copied from org.apache.lucene.spatial.base.LatLng
   *
   * @param a
   * @return
   */
  private static double radians(double a) {
    return a * 0.01745329251994;
  }

  /**
   * copied from org.apache.lucene.spatial.base.LatLng
   *
   * @param longitude
   * @return
   */
  private static double normalizeLongitude(double longitude) {
    double delta = 0;
    if (longitude < 0) delta = 360;
    if (longitude >= 0) delta = -360;

    double normalizedLongitude = longitude;
    while (normalizedLongitude <= -180 || normalizedLongitude >= 180) {
      normalizedLongitude += delta;
    }

    return normalizedLongitude;
  }


}
