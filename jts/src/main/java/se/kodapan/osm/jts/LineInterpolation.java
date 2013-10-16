package se.kodapan.osm.jts;

import com.vividsolutions.jts.geom.Coordinate;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.util.distance.ArcDistance;
import se.kodapan.osm.util.distance.Distance;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds interpolated points along a line if distance between two points within it is too short.
 * <p/>
 * Not thread safe.
 *
 * @author kalle
 * @since 2013-03-25 19:03
 */
public class LineInterpolation {

  private Distance arcDistance = new ArcDistance();


  public List<Coordinate> interpolate(double maximumKilometersPerCoordinate, Way way) {

    List<Coordinate> coordinates = new ArrayList<Coordinate>();
    coordinates.add(new Coordinate(way.getNodes().get(0).getLongitude(), way.getNodes().get(0).getLatitude()));
    Node previousNode = null;
    for (Node node : way.getNodes()) {
      if (previousNode != null) {
        coordinates.addAll(interpolate(maximumKilometersPerCoordinate, node, previousNode));
      }
      previousNode = node;
    }
    return coordinates;

  }

  public List<Coordinate> interpolate(double maximumKilometersPerCoordinate, Coordinate[] coordinates) {

    List<Coordinate> response = new ArrayList<>();
    for (int i = 1; i < coordinates.length; i++) {
      response.addAll(interpolate(maximumKilometersPerCoordinate, coordinates[i - 1], coordinates[i]));
    }
    return response;

  }

  public List<Coordinate> interpolate(double maximumKilometersPerCoordinate, Node a, Node b) {
    return interpolate(maximumKilometersPerCoordinate, new Coordinate(a.getLongitude(), a.getLatitude()), new Coordinate(b.getLongitude(), b.getLatitude()));
  }

  /**
   * Interpolate line between to coordinates so there is one new coordinate every n kilometers
   *
   * @param maximumKilometersPerCoordinate
   * @param a
   * @param b
   * @return
   */
  public List<Coordinate> interpolate(double maximumKilometersPerCoordinate, Coordinate a, Coordinate b) {
    List<com.vividsolutions.jts.geom.Coordinate> line = new ArrayList<com.vividsolutions.jts.geom.Coordinate>();

    double kilometersBetweenNodes = arcDistance.calculate(a.y, a.x, b.y, b.x);
    if (kilometersBetweenNodes > maximumKilometersPerCoordinate) {
      int interpolations = (int) (kilometersBetweenNodes / maximumKilometersPerCoordinate);
      double totalLatitudeDelta = Math.max(a.y, b.y) - Math.min(a.y, b.y);
      double totalLongitudeDelta = Math.max(a.x, b.x) - Math.min(a.x, b.x);
      double latitudeDelta = totalLatitudeDelta / interpolations;
      double longitudeDelta = totalLongitudeDelta / interpolations;
      if (a.y > b.y) {
        latitudeDelta *= -1;
      }
      if (a.x > b.x) {
        longitudeDelta *= -1;
      }

      line.add(a);

      com.vividsolutions.jts.geom.Coordinate previousInterpolation = a;
      for (int i = 0; i < interpolations - 1; i++) {
        com.vividsolutions.jts.geom.Coordinate interpolated = new com.vividsolutions.jts.geom.Coordinate();
        interpolated.y = previousInterpolation.y + latitudeDelta;
        interpolated.x = previousInterpolation.x + longitudeDelta;
        line.add(interpolated);
        previousInterpolation = interpolated;
      }
      if (line.size() == 1) {
        // if interpolation delta is too large then create centroid coordinate
        com.vividsolutions.jts.geom.Coordinate centroid = new com.vividsolutions.jts.geom.Coordinate();
        centroid.y = (a.y + b.x) / 2d;
        centroid.x = (a.x + b.y) / 2d;
        line.add(centroid);
      }
      line.add(b);

    } else {
      line.add(a);
      line.add(b);

    }

    return line;
  }

}