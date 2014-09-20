package se.kodapan.osm.domain.root.indexed;

/**
 * Created by kalle on 10/19/13.
 */
public abstract class WayEnvelopeQueryFactory<Query> extends QueryFactory<Query> {

  private double southLatitude;
  private double westLongitude;
  private double northLatitude;
  private double eastLongitude;

  public double getSouthLatitude() {
    return southLatitude;
  }

  public WayEnvelopeQueryFactory<Query> setSouthLatitude(double southLatitude) {
    this.southLatitude = southLatitude;
    return this;
  }

  public double getWestLongitude() {
    return westLongitude;
  }

  public WayEnvelopeQueryFactory<Query> setWestLongitude(double westLongitude) {
    this.westLongitude = westLongitude;
    return this;
  }

  public double getNorthLatitude() {
    return northLatitude;
  }

  public WayEnvelopeQueryFactory<Query> setNorthLatitude(double northLatitude) {
    this.northLatitude = northLatitude;
    return this;
  }

  public double getEastLongitude() {
    return eastLongitude;
  }

  public WayEnvelopeQueryFactory<Query> setEastLongitude(double eastLongitude) {
    this.eastLongitude = eastLongitude;
    return this;
  }

}
