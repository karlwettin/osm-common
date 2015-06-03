package se.kodapan.osm.jts;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import se.kodapan.osm.domain.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates JTS geometries out of OSM nodes, ways and relations.
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

  public MultiPolygon createMultiPolygon(Relation relation) {

    List<LinearRing> linearRings = new ArrayList<LinearRing>();

    List<Node> nodes = new ArrayList<Node>();
    Node firstNode = null;
    for (RelationMembership membership : relation.getMembers()) {

      if (!"outer".equalsIgnoreCase(membership.getRole())) {
        continue;
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

      nodes.addAll(membership.getObject().accept(new OsmObjectVisitor<List<Node>>() {
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
          List<Node> nodes = new ArrayList<Node>();
          for (RelationMembership membership : relation.getMembers()) {
            nodes.addAll(membership.getObject().accept(this));
          }
          return nodes;
        }
      }));

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
}
