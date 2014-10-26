package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.search.Query;

/**
 * @author kalle
 * @since 2014-10-26 01:38
 */
public class NodeRadialEnvelopeQueryFactoryImpl extends NodeRadialEnvelopeQueryFactory<Query> {

  @Override
  public Query build() {

    double south = Double.MAX_VALUE;
    double west = Double.MAX_VALUE;
    double east = Double.MIN_VALUE;
    double north = Double.MIN_VALUE;

    double radiusLatitude = (getKilometerRadius() / 6378.8d) * (180 / Math.PI);
    double radiusLongitude = radiusLatitude / Math.cos(getLatitude() * (Math.PI / 180));

    int step = (int) (360d / (double) 100);
//    for (int i = 0; i <= 361; i+= step) {
    for (int i = 0; i < 360; i += step) {
      double a = i * (Math.PI / 180);
      double latitude = getLatitude() + (radiusLatitude * Math.sin(a));
      double longitude = getLongitude() + (radiusLongitude * Math.cos(a));

      if (latitude < south) {
        south = latitude;
      }
      if (latitude > north) {
        north = latitude;
      }
      if (longitude < west) {
        west = longitude;
      }
      if (longitude > east) {
        east = longitude;
      }
    }

    return new NodeEnvelopeQueryFactoryImpl()
        .setSouthLatitude(south)
        .setWestLongitude(west)
        .setNorthLatitude(north)
        .setEastLongitude(east)
        .build();

  }


}
