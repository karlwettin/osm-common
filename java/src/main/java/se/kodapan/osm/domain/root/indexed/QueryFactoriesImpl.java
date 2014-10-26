package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.search.Query;

/**
 * @author kalle
 * @since 2013-10-21 03:08
 */
public class QueryFactoriesImpl extends QueryFactories<Query> {

  @Override
  public ContainsTagKeyAndValueQueryFactory<Query> containsTagKeyAndValueQueryFactory() {
    return new ContainsTagKeyAndValueQueryFactoryImpl();
  }

  @Override
  public ContainsTagKeyQueryFactory<Query> containsTagKeyQueryFactory() {
    return new ContainsTagKeyQueryFactoryImpl();
  }

  @Override
  public ContainsTagValueQueryFactory<Query> containsTagValueQueryFactory() {
    return new ContainsTagValueQueryFactoryImpl();
  }

  @Override
  public NodeEnvelopeQueryFactory<Query> nodeEnvelopeQueryFactory() {
    return new NodeEnvelopeQueryFactoryImpl();
  }

  @Override
  public NodeRadialEnvelopeQueryFactory<Query> nodeRadialEnvelopeQueryFactory() {
    return new NodeRadialEnvelopeQueryFactoryImpl();
  }

  @Override
  public WayEnvelopeQueryFactory<Query> wayEnvelopeQueryFactory() {
    return new WayEnvelopeQueryFactoryImpl();
  }

  @Override
  public WayRadialEnvelopeQueryFactory<Query> wayRadialEnvelopeQueryFactory() {
    return new WayRadialEnvelopeQueryFactoryImpl();
  }

}
