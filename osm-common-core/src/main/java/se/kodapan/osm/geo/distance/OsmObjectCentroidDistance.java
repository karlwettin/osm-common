package se.kodapan.osm.geo.distance;

import se.kodapan.osm.domain.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Evaluates the distance between the centroid of two OSM objects.
 *
 * @author kalle
 * @since 2013-07-29 15:28
 */
public class OsmObjectCentroidDistance
    implements OsmObjectVisitor<double[]>, OsmObjectDistance {

  private Distance distance;

  public OsmObjectCentroidDistance(Distance distance) {
    this.distance = distance;
  }

  @Override
  public double calculate(OsmObject a, OsmObject b) {
    double[] latLonA = a.accept(this);
    double[] latLonB = b.accept(this);
    return distance.calculate(latLonA[0], latLonA[1], latLonB[0], latLonB[1]);
  }


  @Override
  public double[] visit(Node node) {
    return new double[]{node.getLatitude(), node.getLongitude()};
  }

  @Override
  public double[] visit(Way way) {
    return evaluateCentroid(way.getNodes());
  }

  @Override
  public double[] visit(Relation relation) {
    final Set<Node> nodes = new HashSet<Node>();
    OsmObjectVisitor<Void> visitor = new OsmObjectVisitor<Void>() {
      @Override
      public Void visit(Node node) {
        nodes.add(node);
        return null;
      }

      @Override
      public Void visit(Way way) {
        nodes.addAll(way.getNodes());
        return null;
      }

      private Set<Relation> visitedRelations = new HashSet<Relation>();

      @Override
      public Void visit(Relation relation) {
        if (visitedRelations.add(relation)) {
          relation.accept(this);
        }
        return null;
      }
    };
    for (RelationMembership membership : relation.getMembers()) {
      membership.getObject().accept(visitor);
    }
    return evaluateCentroid(nodes);
  }

  private double[] evaluateCentroid(Collection<Node> nodes) {
    double size = (double) nodes.size();
    double latitude = 0d;
    double longitude = 0d;
    for (Node node : nodes) {
      latitude += node.getLatitude() / size;
      longitude += node.getLongitude() / size;
    }
    return new double[]{latitude, longitude};
  }
}
