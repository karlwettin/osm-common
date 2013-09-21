package se.kodapan.osm.domain.root;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.OsmObjectVisitor;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;

/**
 * Returns the negated values of a decorated filter.
 *
* @author kalle
* @since 2013-07-27 21:42
*/
public class InvertedFilter implements OsmObjectVisitor<Boolean> {

  private OsmObjectVisitor<Boolean> decorated;

  public InvertedFilter(OsmObjectVisitor<Boolean> decorated) {
    this.decorated = decorated;
  }

  @Override
  public Boolean visit(Node node) {
    return !decorated.visit(node);
  }

  @Override
  public Boolean visit(Way way) {
    return !decorated.visit(way);
  }

  @Override
  public Boolean visit(Relation relation) {
    return !decorated.visit(relation);
  }
}
