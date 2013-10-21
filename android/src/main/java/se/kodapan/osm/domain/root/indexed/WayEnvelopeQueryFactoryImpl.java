package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

/**
 * Created by kalle on 10/19/13.
 */
public class WayEnvelopeQueryFactoryImpl extends WayEnvelopeQueryFactory<Query> {


  public Query build() {

    if (getSouthLatitude() >= getNorthLatitude()) {
      throw new IllegalArgumentException("south must be less than north");
    }
    if (getWestLongitude() >= getEastLongitude()) {
      throw new IllegalArgumentException("west must be less than east");
    }

    BooleanQuery bq = new BooleanQuery();

    BooleanQuery sw = new BooleanQuery();
    sw.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.south_latitude", 4, getSouthLatitude(), getNorthLatitude(), true, true), BooleanClause.Occur.MUST));
    sw.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.west_longitude", 4, getWestLongitude(), getEastLongitude(), true, true), BooleanClause.Occur.MUST));
    bq.add(sw, BooleanClause.Occur.SHOULD);

    BooleanQuery se = new BooleanQuery();
    se.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.south_latitude", 4, getSouthLatitude(), getNorthLatitude(), true, true), BooleanClause.Occur.MUST));
    se.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.east_longitude", 4, getWestLongitude(), getEastLongitude(), true, true), BooleanClause.Occur.MUST));
    bq.add(se, BooleanClause.Occur.SHOULD);

    BooleanQuery ne = new BooleanQuery();
    ne.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.north_latitude", 4, getSouthLatitude(), getNorthLatitude(), true, true), BooleanClause.Occur.MUST));
    ne.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.east_longitude", 4, getWestLongitude(), getEastLongitude(), true, true), BooleanClause.Occur.MUST));
    bq.add(ne, BooleanClause.Occur.SHOULD);

    BooleanQuery nw = new BooleanQuery();
    nw.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.north_latitude", 4, getSouthLatitude(), getNorthLatitude(), true, true), BooleanClause.Occur.MUST));
    nw.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.west_longitude", 4, getWestLongitude(), getEastLongitude(), true, true), BooleanClause.Occur.MUST));
    bq.add(nw, BooleanClause.Occur.SHOULD);

    return bq;
  }

}
