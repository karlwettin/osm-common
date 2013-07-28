package se.kodapan.osm.domain.root;

import se.kodapan.osm.domain.*;

/**
* @author kalle
* @since 2013-07-27 21:42
*/
public abstract  class AbstractTagFilter implements OsmObjectVisitor<Boolean> {


  public abstract Boolean visit(OsmObject tagged);

  @Override
  public Boolean visit(Node node) {
    return visit((OsmObject) node);
  }

  @Override
  public Boolean visit(Way way) {
    return visit((OsmObject) way);
  }

  @Override
  public Boolean visit(Relation relation) {
    return visit((OsmObject) relation);
  }
}
