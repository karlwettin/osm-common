package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

/**
 * Created by kalle on 10/19/13.
 */
public class WayEnvelopeQueryFactory {

  private double southLatitude;
  private double westLongitude;
  private double northLatitude;
  private double eastLongitude;

  public double getSouthLatitude() {
    return southLatitude;
  }

  public WayEnvelopeQueryFactory setSouthLatitude(double southLatitude) {
    this.southLatitude = southLatitude;
    return this;
  }

  public double getWestLongitude() {
    return westLongitude;
  }

  public WayEnvelopeQueryFactory setWestLongitude(double westLongitude) {
    this.westLongitude = westLongitude;
    return this;
  }

  public double getNorthLatitude() {
    return northLatitude;
  }

  public WayEnvelopeQueryFactory setNorthLatitude(double northLatitude) {
    this.northLatitude = northLatitude;
    return this;
  }

  public double getEastLongitude() {
    return eastLongitude;
  }

  public WayEnvelopeQueryFactory setEastLongitude(double eastLongitude) {
    this.eastLongitude = eastLongitude;
    return this;
  }

  public Query build() {

    if (southLatitude >= northLatitude) {
      throw new IllegalArgumentException("south must be less than north");
    }
    if (westLongitude >= eastLongitude) {
      throw new IllegalArgumentException("west must be less than east");
    }

    BooleanQuery bq = new BooleanQuery();

    BooleanQuery sw = new BooleanQuery();
    sw.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.south_latitude", 4, southLatitude, northLatitude, true, true), BooleanClause.Occur.MUST));
    sw.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.west_longitude", 4, westLongitude, eastLongitude, true, true), BooleanClause.Occur.MUST));
    bq.add(sw, BooleanClause.Occur.SHOULD);

    BooleanQuery se = new BooleanQuery();
    se.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.south_latitude", 4, southLatitude, northLatitude, true, true), BooleanClause.Occur.MUST));
    se.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.east_longitude", 4, westLongitude, eastLongitude, true, true), BooleanClause.Occur.MUST));
    bq.add(se, BooleanClause.Occur.SHOULD);

    BooleanQuery ne = new BooleanQuery();
    ne.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.north_latitude", 4, southLatitude, northLatitude, true, true), BooleanClause.Occur.MUST));
    ne.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.east_longitude", 4, westLongitude, eastLongitude, true, true), BooleanClause.Occur.MUST));
    bq.add(ne, BooleanClause.Occur.SHOULD);

    BooleanQuery nw = new BooleanQuery();
    nw.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.north_latitude", 4, southLatitude, northLatitude, true, true), BooleanClause.Occur.MUST));
    nw.add(new BooleanClause(NumericRangeQuery.newDoubleRange("way.envelope.west_longitude", 4, westLongitude, eastLongitude, true, true), BooleanClause.Occur.MUST));
    bq.add(nw, BooleanClause.Occur.SHOULD);

    return bq;
  }

}
