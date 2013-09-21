package se.kodapan.osm.domain;

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
