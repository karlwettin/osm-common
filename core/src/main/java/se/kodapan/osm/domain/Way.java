package se.kodapan.osm.domain;

import se.kodapan.osm.domain.root.NotLoadedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2013-05-01 15:42
 */
public class Way extends OsmObject implements Serializable {

  private static final long serialVersionUID = 1l;

  @Override
  public <R> R accept(OsmObjectVisitor<R> visitor) {
    return visitor.visit(this);
  }

  private List<Node> nodes;

  /**
   * @return true if an enclosed polygon
   */
  public boolean isPolygon() {
    if (!isLoaded()) {
      throw new NotLoadedException(this);
    }
    return getNodes().size() > 2 && getNodes().get(0).equals(getNodes().get(getNodes().size() - 1));
  }

  public void addNode(Node node) {
    if (nodes == null) {
      nodes = new ArrayList<Node>(50);
    }
    nodes.add(node);
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public void setNodes(List<Node> nodes) {
    this.nodes = nodes;
  }

  @Override
  public String toString() {
    return "Way{" +
        super.toString() +
        "nodes.size=" + (nodes == null ? "null" : nodes.size()) +
        '}';
  }
}
