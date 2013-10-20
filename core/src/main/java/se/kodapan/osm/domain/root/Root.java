package se.kodapan.osm.domain.root;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.OsmObject;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;

import java.io.Serializable;
import java.util.Set;

/**
 * @author kalle
 * @since 2013-10-20 04:35
 */
public abstract interface Root {

  public abstract void add(OsmObject osmObject);

  public abstract Node getNode(long identity);
  public abstract Way getWay(long identity);
  public abstract Relation getRelation(long identity);

  public abstract Enumerator<Node> enumerateNodes();
  public abstract Enumerator<Way> enumerateWays();
  public abstract Enumerator<Relation> enumerateRelations();

  public abstract Set<OsmObject> remove(OsmObject osmObject);
  public abstract Node removeNode(long identity);
  public abstract Way removeWay(long identity);
  public abstract Relation removeRelation(long identity);

  public static abstract class Enumerator<T> {
    public abstract T next();
  }

}
