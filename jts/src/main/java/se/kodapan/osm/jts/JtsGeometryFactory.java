package se.kodapan.osm.jts;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import se.kodapan.osm.domain.*;

import java.util.*;

/**
 * Creates JTS geometries out of OSM nodes, ways and relations.
 *
 * This is half baked. Consider using some tool to convert OSM to GeoJSON and add JTS GeoJsonReader dependency.
 *
 * @author kalle
 * @since 2015-06-03 03:22
 */
public class JtsGeometryFactory {

  private GeometryFactory geometryFactory;

  public JtsGeometryFactory() {
    this(new GeometryFactory());
  }

  public JtsGeometryFactory(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public Point createPoint(Node node) {
    return geometryFactory.createPoint(new Coordinate(node.getX(), node.getY()));
  }

  public LineString createLineString(Way way) {

    Coordinate[] coordinates = new Coordinate[way.getNodes().size()];
    List<Node> nodes = way.getNodes();
    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.get(i);
      coordinates[i] = new Coordinate(node.getX(), node.getY());
    }

    if (!way.isPolygon()) {
      return geometryFactory.createLineString(coordinates);
    } else {
      throw new RuntimeException("Way expected not to be a polygon.");
    }

  }

  public Polygon createPolygon(Way way) {

    Coordinate[] coordinates = new Coordinate[way.getNodes().size()];
    List<Node> nodes = way.getNodes();
    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.get(i);
      coordinates[i] = new Coordinate(node.getX(), node.getY());
    }

    if (!way.isPolygon()) {
      throw new RuntimeException("Way expected to be a polygon.");
    } else {
      return geometryFactory.createPolygon(geometryFactory.createLinearRing(coordinates), null);
    }

  }

  /**
   * Asserts that relation members are all outer ways that form a single polygon.
   *
   * @param relation
   * @return
   */
  public Polygon createOuterWaysPolygon(Relation relation) {

    List<List<Coordinate>> lines = new ArrayList<List<Coordinate>>(relation.getMembers().size());
    for (RelationMembership member : relation.getMembers()) {
      if (!"outer".equalsIgnoreCase(member.getRole())) {
        throw new RuntimeException();
      }
      Way way = (Way)member.getObject();
      List<Coordinate> line = new ArrayList<Coordinate>(way.getNodes().size());
      for (Node node : way.getNodes()) {
        line.add(new Coordinate(node.getX(), node.getY()));
      }
      lines.add(line);
    }

    int maxIterations = lines.size();

    List<List<Coordinate>> sorted = new ArrayList<List<Coordinate>>(lines.size());
    sorted.add(lines.remove(0));

    int iterations = 0;
    while (!lines.isEmpty()) {
      if (iterations++ >= maxIterations) {
        throw new RuntimeException("Eternal loop");
      }
      for (Iterator<List<Coordinate>> lineIterator = lines.iterator(); lineIterator.hasNext();) {
        List<Coordinate> line = lineIterator.next();

        for (List<Coordinate> testLine : new ArrayList<List<Coordinate>>(sorted)) {
          if (testLine.get(testLine.size() -1).equals(line.get(0))) {
            sorted.add(line);
            lineIterator.remove();
            break;

          } else if (testLine.get(testLine.size() -1).equals(line.get(line.size() -1))) {
            Collections.reverse(line);
            sorted.add(line);
            lineIterator.remove();
            break;

          }
        }

      }
    }

    int coordinatesCount = 0;
    for (List<Coordinate> line : sorted) {
      coordinatesCount+=line.size();
    }
    int position = 0;
    Coordinate[] coordinates = new Coordinate[coordinatesCount];
    for (List<Coordinate> line : sorted) {
      for (Coordinate coordinate : line) {
        coordinates[position++] = coordinate;
      }
    }
    return geometryFactory.createPolygon(geometryFactory.createLinearRing(coordinates), null);


  }


  /**
   * Asserts that relation members are in order and the same direction.
   * Does not support polygons with holes (role=inner)
   *
   * @param relation
   * @return
   */
  public MultiPolygon createMultiPolygon(Relation relation) {

    List<LinearRing> linearRings = new ArrayList<LinearRing>();

    List<Node> nodes = new ArrayList<Node>();
    Node firstNode = null;
    for (RelationMembership membership : relation.getMembers()) {

      if (!"outer".equalsIgnoreCase(membership.getRole())) {
        continue; // todo inner as holes!
      }

      if (firstNode == null) {
        firstNode = membership.getObject().accept(new OsmObjectVisitor<Node>() {
          @Override
          public Node visit(Node node) {
            return node;
          }

          @Override
          public Node visit(Way way) {
            return way.getNodes().get(0);
          }

          @Override
          public Node visit(Relation relation) {
            return relation.accept(this);
          }
        });
      }

      List<Node> nextNodes = membership.getObject().accept(new NodesCollector());
      if (nodes.isEmpty()) {
        nodes.addAll(nextNodes);

      } else {
        Node previousNode = nodes.get(nodes.size() - 1);
        if (nextNodes.get(0) == previousNode) {
          nodes.addAll(nextNodes);

        } else if (nextNodes.get(nextNodes.size() -1) == previousNode) {
          Collections.reverse(nextNodes);
          nodes.addAll(nextNodes);

        } else {
          throw new RuntimeException("Non connected members in relation");
        }
      }


      if (nodes.get(nodes.size() - 1).equals(firstNode)) {
        Coordinate[] coordinates = new Coordinate[nodes.size() + 1];
        for (int i = 0; i < nodes.size(); i++) {
          Node node = nodes.get(i);
          coordinates[i] = new Coordinate(node.getX(), node.getY());
        }
        coordinates[coordinates.length - 1] = coordinates[0];
        linearRings.add(new LinearRing(new CoordinateArraySequence(coordinates), geometryFactory));
        firstNode = null;
        nodes.clear();
      }
    }

    Polygon[] polygons = new Polygon[linearRings.size()];
    for (int i = 0; i < linearRings.size(); i++) {
      polygons[i] = new Polygon(linearRings.get(i), null, geometryFactory);
    }

    return geometryFactory.createMultiPolygon(polygons);


  }


  private Comparator<List<Node>> linesComparator = new Comparator<List<Node>>() {
    @Override
    public int compare(List<Node> l1, List<Node> l2) {
      if (coordinateEquals(l1.get(0), l2.get(l2.size() - 1))) {
        return -1;
      } else if (coordinateEquals(l1.get(l1.size() - 1), l2.get(0))) {
        return 1;
      } else {
        return 0;
      }
    }

    private boolean coordinateEquals(Node n1, Node n2) {
      return n1.getLatitude() == n2.getLatitude()
          && n1.getLongitude() == n2.getLongitude();
    }
  };

  private class NodesCollector implements OsmObjectVisitor<List<Node>> {

    @Override
    public List<Node> visit(Node node) {
      ArrayList<Node> nodes = new ArrayList<Node>(1);
      nodes.add(node);
      return nodes;
    }

    @Override
    public List<Node> visit(Way way) {
      return way.getNodes();
    }

    @Override
    public List<Node> visit(Relation relation) {

      List<List<Node>> lines = new ArrayList<List<Node>>();

      for (RelationMembership membership : relation.getMembers()) {
        lines.add(membership.getObject().accept(new NodesCollector()));
      }

      Collections.sort(lines, linesComparator);

      int nodesCount = 0;
      for (List<Node> line : lines) {
        nodesCount += line.size();
      }
      List<Node> nodes = new ArrayList<Node>(nodesCount);
      for (List<Node> line : lines) {
        nodes.addAll(line);
      }
      return nodes;
    }
  }




}
