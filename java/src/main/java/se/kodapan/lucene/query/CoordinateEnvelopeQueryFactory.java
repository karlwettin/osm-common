package se.kodapan.lucene.query;

import org.apache.lucene.search.*;

/**
 * @author kalle
 * @since 2014-09-08 14:32
 */
public class CoordinateEnvelopeQueryFactory {

  private String latitudeField;
  private String longitudeField;

  private Double south;
  private Double west;
  private Double north;
  private Double east;

  public CoordinateEnvelopeQueryFactory setLatitudeField(String latitudeField) {
    this.latitudeField = latitudeField;
    return this;
  }

  public CoordinateEnvelopeQueryFactory setLongitudeField(String longitudeField) {
    this.longitudeField = longitudeField;
    return this;
  }

  public CoordinateEnvelopeQueryFactory setSouth(Double south) {
    this.south = south;
    return this;
  }

  public CoordinateEnvelopeQueryFactory setWest(Double west) {
    this.west = west;
    return this;
  }

  public CoordinateEnvelopeQueryFactory setNorth(Double north) {
    this.north = north;
    return this;
  }

  public CoordinateEnvelopeQueryFactory setEast(Double east) {
    this.east = east;
    return this;
  }

  public String getLatitudeField() {
    return latitudeField;
  }

  public String getLongitudeField() {
    return longitudeField;
  }

  public Double getSouth() {
    return south;
  }

  public Double getWest() {
    return west;
  }

  public Double getNorth() {
    return north;
  }

  public Double getEast() {
    return east;
  }

  public Query build() {
    if (south <= -90d
        && west <= -180d
        && north >= 90d
        && east >= 180d) {
      return new MatchAllDocsQuery();
    }

    BooleanQuery query = new BooleanQuery();

    query.add(NumericRangeQuery.newDoubleRange(latitudeField, south, north, true, true), BooleanClause.Occur.MUST);

    if (west < east) {

      query.add(NumericRangeQuery.newDoubleRange(longitudeField, west, east, true, true), BooleanClause.Occur.MUST);

    } else {

      BooleanQuery longitudeQuery = new BooleanQuery();

      longitudeQuery.add(NumericRangeQuery.newDoubleRange(longitudeField, -180d, west, true, true), BooleanClause.Occur.SHOULD);
      longitudeQuery.add(NumericRangeQuery.newDoubleRange(longitudeField, east, 180d, true, true), BooleanClause.Occur.SHOULD);

      query.add(longitudeQuery, BooleanClause.Occur.MUST);


    }

    return query;

  }


}
