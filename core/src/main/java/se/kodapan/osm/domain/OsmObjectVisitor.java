package se.kodapan.osm.domain;

/**
 * @author kalle
 * @since 2013-05-01 16:49
 */
public interface OsmObjectVisitor<R> {

  public R visit(Node node);

  public R visit(Way way);

  public R visit(Relation relation);

}
