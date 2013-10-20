package se.kodapan.osm.domain.root;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;

import java.io.Serializable;

/**
 * @author kalle
 * @since 2013-10-20 04:38
 */
public abstract class AbstractRoot implements Root, Serializable {

  private static final long serialVersionUID = 1l;


  @Override
  public Node removeNode(long identity) {
    Node node = getNode(identity);
    remove(node);
    return node;
  }

  @Override
  public Way removeWay(long identity) {
    Way way = getWay(identity);
    remove(way);
    return way;
  }

  @Override
  public Relation removeRelation(long identity) {
    Relation relation = getRelation(identity);
    remove(relation);
    return relation;
  }

}
