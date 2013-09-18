package se.kodapan.osm.geo.distance;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.jts.LineInterpolation;

/**
 * True arc distance metrics, returning distance between coordinates in kilometers.
 *
 * Based on code available in org.apache.lucene.spatial.base.LatLng.
 *
 * @author kalle
 * @since 2013-07-29 15:20
 */
public class ArcDistance  extends Distance {

  public double calculate(Polygon polygon, Node node, double precisionKilometers, GeometryFactory geometryFactory) {
    return calculate(polygon, new Coordinate(node.getX(), node.getY()), precisionKilometers, geometryFactory);
  }

  public double calculate(Polygon polygon, Coordinate coordinate, double precisionKilometers, GeometryFactory geometryFactory) {
    Point point = new Point(new CoordinateArraySequence(new Coordinate[]{coordinate}), geometryFactory);
    if (polygon.contains(point)) {
      // todo distance to border? well if that should be the case then factor this method out of this class!
      return 0;
    }

    double smallestDistance = Double.MAX_VALUE;
    Coordinate[] coordinates = polygon.getCoordinates();
    for (int i = 1; i < coordinates.length; i++) {
      for (Coordinate interpolated : new LineInterpolation().interpolate(precisionKilometers, coordinates[i-1], coordinates[i])) {
        double distance = calculate(interpolated, coordinate);
        if (distance < smallestDistance) {
          smallestDistance = distance;
        }
      }
    }
    return smallestDistance;
  }

  public double calculate(Node a, Node b) {
    return calculate(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
  }

  public double calculate(Coordinate a, Coordinate b) {
    return calculate(a.y, a.x, b.y, b.x);
  }

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
  private double radians(double a) {
    return a * 0.01745329251994;
  }

  /**
   * copied from org.apache.lucene.spatial.base.LatLng
   *
   * @param longitude
   * @return
   */
  private double normalizeLongitude(double longitude) {
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
