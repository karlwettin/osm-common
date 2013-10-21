package se.kodapan.osm.domain.root.indexed;

/**
 * The reason for this to exist is that different platforms use different versions of Lucene.
 *
 * @author kalle
 * @since 2013-10-21 02:46
 */
public abstract class QueryFactories<Query> {

  public abstract ContainsTagKeyAndValueQueryFactory<Query> containsTagKeyAndValueQueryFactory();

  public abstract ContainsTagKeyQueryFactory<Query> containsTagKeyQueryFactory();

  public abstract ContainsTagValueQueryFactory<Query> containsTagValueQueryFactory();

  public abstract NodeEnvelopeQueryFactory<Query> nodeEnvelopeQueryFactory();

  public abstract WayEnvelopeQueryFactory<Query> wayEnvelopeQueryFactory();

}
