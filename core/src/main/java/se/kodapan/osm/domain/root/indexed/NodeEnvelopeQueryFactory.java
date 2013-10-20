package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

/**
 * Created by kalle on 10/19/13.
 */
public class NodeEnvelopeQueryFactory {

  private double southLatitude;
  private double westLongitude;
  private double northLatitude;
  private double eastLongitude;

  public double getSouthLatitude() {
    return southLatitude;
  }

  public NodeEnvelopeQueryFactory setSouthLatitude(double southLatitude) {
    this.southLatitude = southLatitude;
    return this;
  }

  public double getWestLongitude() {
    return westLongitude;
  }

  public NodeEnvelopeQueryFactory setWestLongitude(double westLongitude) {
    this.westLongitude = westLongitude;
    return this;
  }

  public double getNorthLatitude() {
    return northLatitude;
  }

  public NodeEnvelopeQueryFactory setNorthLatitude(double northLatitude) {
    this.northLatitude = northLatitude;
    return this;
  }

  public double getEastLongitude() {
    return eastLongitude;
  }

  public NodeEnvelopeQueryFactory setEastLongitude(double eastLongitude) {
    this.eastLongitude = eastLongitude;
    return this;
  }

  public Query build() {
    BooleanQuery bq = new BooleanQuery();
    bq.add(new BooleanClause(NumericRangeQuery.newDoubleRange("node.latitude", 4, southLatitude, northLatitude, true, true), BooleanClause.Occur.MUST));
    bq.add(new BooleanClause(NumericRangeQuery.newDoubleRange("node.longitude", 4, westLongitude, eastLongitude, true, true), BooleanClause.Occur.MUST));
    return bq;
  }

}
