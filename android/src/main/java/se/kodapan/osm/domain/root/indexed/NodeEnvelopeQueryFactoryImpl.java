package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

/**
 * Created by kalle on 10/19/13.
 */
public class NodeEnvelopeQueryFactoryImpl extends NodeEnvelopeQueryFactory<Query> {

  public Query build() {
    BooleanQuery bq = new BooleanQuery();
    bq.add(new BooleanClause(NumericRangeQuery.newDoubleRange("node.latitude", 4, getSouthLatitude(), getNorthLatitude(), true, true), BooleanClause.Occur.MUST));
    bq.add(new BooleanClause(NumericRangeQuery.newDoubleRange("node.longitude", 4, getWestLongitude(), getEastLongitude(), true, true), BooleanClause.Occur.MUST));
    return bq;
  }

}
