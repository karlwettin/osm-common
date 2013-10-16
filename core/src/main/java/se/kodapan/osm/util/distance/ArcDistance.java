package se.kodapan.osm.util.distance;

/**
 * True arc distance metrics, returning distance between coordinates in kilometers.
 *
 * Based on code available in org.apache.lucene.spatial.base.LatLng.
 *
 * @author kalle
 * @since 2013-07-29 15:20
 */
public class ArcDistance  extends Distance {

  /**
   * copied from org.apache.lucene.spatial.base.LatLng
   * @param latitudeA
   * @param longitudeA
   * @param latitudeB
   * @param longitudeB
   * @return distance in kilometers
   */
  @Override
  public double calculate(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {


    double dLatitude = Math.toRadians(latitudeB-latitudeA);
    double dLongitude = Math.toRadians(longitudeB-longitudeA);
    double a = Math.sin(dLatitude/2) * Math.sin(dLatitude/2) +
        Math.cos(Math.toRadians(latitudeA)) * Math.cos(Math.toRadians(latitudeB)) *
            Math.sin(dLongitude/2) * Math.sin(dLongitude/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    double radiusEarth = 6371; // km
    double distance = radiusEarth * c;

    return distance;
  }



}
