package se.kodapan.osm.domain.root.indexed;

/**
 * Created by kalle on 10/19/13.
 */
public abstract class WayRadialEnvelopeQueryFactory<Query> extends QueryFactory<Query> {

  private double latitude;
  private double longitude;

  private double kilometerRadius;

  public double getLatitude() {
    return latitude;
  }

  public WayRadialEnvelopeQueryFactory<Query> setLatitude(double latitude) {
    this.latitude = latitude;
    return this;
  }

  public double getLongitude() {
    return longitude;
  }

  public WayRadialEnvelopeQueryFactory<Query> setLongitude(double longitude) {
    this.longitude = longitude;
    return this;
  }

  public double getKilometerRadius() {
    return kilometerRadius;
  }

  public WayRadialEnvelopeQueryFactory<Query> setKilometerRadius(double kilometerRadius) {
    this.kilometerRadius = kilometerRadius;
    return this;
  }
}
