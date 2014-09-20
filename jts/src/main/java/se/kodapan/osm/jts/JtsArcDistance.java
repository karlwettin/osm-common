package se.kodapan.osm.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.util.distance.ArcDistance;

/**
 * @author kalle
 * @since 2013-09-21 3:49 PM
 */
public class JtsArcDistance extends ArcDistance {

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
      for (Coordinate interpolated : new LineInterpolation().interpolate(precisionKilometers, coordinates[i - 1], coordinates[i])) {
        double distance = calculate(interpolated, coordinate);
        if (distance < smallestDistance) {
          smallestDistance = distance;
        }
      }
    }
    return smallestDistance;
  }

  public double calculate(Coordinate a, Coordinate b) {
    return calculate(a.y, a.x, b.y, b.x);
  }


}
