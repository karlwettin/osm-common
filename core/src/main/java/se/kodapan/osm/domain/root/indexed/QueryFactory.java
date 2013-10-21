package se.kodapan.osm.domain.root.indexed;

/**
 * @author kalle
 * @since 2013-10-21 02:49
 */
public abstract class QueryFactory<Query> {

  public abstract Query build();

}
