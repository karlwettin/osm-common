package se.kodapan.osm.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import se.kodapan.osm.domain.Node;

public class CircleOp {

  public CircleOp() {
    this(new GeometryFactory());
  }

  private GeometryFactory geometryFactory;

  public CircleOp(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public Polygon circle(Point point, double radiusKilometers, int circumferenceResolution) {
    return circle(point.getCoordinate(), radiusKilometers, circumferenceResolution);
  }

  public Polygon circle(Coordinate coordinate, double radiusKilometers, int circumferenceResolution) {
    return circle(coordinate.x, coordinate.y, radiusKilometers, circumferenceResolution);
  }

  public Polygon circle(Node node, double radiusKilometers, int circumferenceResolution) {
    return circle(node.getLongitude(), node.getLatitude(), radiusKilometers, circumferenceResolution);
  }

  public Polygon circle(double centroidLongitude, double centroidLatitude, double radiusKilometers, int circumferenceResolution) {
    Coordinate[] coordinates = new Coordinate[circumferenceResolution + 1];

    double radiusLatitude = (radiusKilometers / 6378.8d) * (180 / Math.PI);
    double radiusLongitude = radiusLatitude / Math.cos(centroidLatitude * (Math.PI / 180));

    int index = 0;
    int step = (int) (360d / (double) circumferenceResolution);
    for (int i = 0; i < 360; i += step) {
      double a = i * (Math.PI / 180);
      double latitude = centroidLatitude + (radiusLatitude * Math.sin(a));
      double longitude = centroidLongitude + (radiusLongitude * Math.cos(a));
      coordinates[index++] = new Coordinate(longitude, latitude);
    }
    coordinates[coordinates.length - 1] = coordinates[0];

    return geometryFactory.createPolygon(geometryFactory.createLinearRing(coordinates), null);
  }

  /**
   * Calculates an approximated number of kilometers radius one have to draw at a given latitude
   * in order to create a circle that equals the same amount of pixels as if it was drawn at the equator
   * when using 3857 projection.
   *
   * This was implemented to allow for equal pixel spacing between place names on a map.
   *
   *
   *
   * @param kmRadiusAtEquator
   * @param latitude
   * @return
   */
  public static double calculateEvenRadiusKilometers(double kmRadiusAtEquator, double latitude) {
    if (latitude < 0) {
      latitude *= -1;
    }
    return kmRadiusAtEquator * (-0.009897 * latitude + 1);
  }

  public Polygon circleEquatorEquality(Point point, double radiusKilometersAtEquator, int circumferenceResolution) {
    return circleEquatorEquality(point.getCoordinate(), radiusKilometersAtEquator, circumferenceResolution);

  }
  public Polygon circleEquatorEquality(Coordinate coordinate, double radiusKilometersAtEquator, int circumferenceResolution) {
    return circleEquatorEquality(coordinate.x, coordinate.y, radiusKilometersAtEquator, circumferenceResolution);

  }
  public Polygon circleEquatorEquality(double centroidLongitude, double centroidLatitude, double radiusKilometersAtEquator, int circumferenceResolution) {
    return circle(centroidLongitude, centroidLatitude, calculateEvenRadiusKilometers(radiusKilometersAtEquator, centroidLatitude), circumferenceResolution);
  }
}
