package se.kodapan.osm.util;

/**
 * @author kalle
 * @since 2013-12-31 02:38
 */
public class Envelope {

  public Coordinate southwest;
  private Coordinate northeast;

  public Envelope() {
  }

  public Envelope(Coordinate centroid, double kilometersWidth) {
    long width = (long) (6378.1d / kilometersWidth);
    long height = width;

    double latitudeStep = 180d / (double) height;
    double longitudeStep = 360d / (double) width;

    northeast = new Coordinate(centroid.getLatitude() - latitudeStep, centroid.getLongitude() + longitudeStep);
    southwest = new Coordinate(centroid.getLatitude() + latitudeStep, centroid.getLongitude() - latitudeStep);
  }

  public Envelope(Coordinate southwest, Coordinate northeast) {
    this.southwest = southwest;
    this.northeast = northeast;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Envelope envelope = (Envelope) o;

    if (northeast != null ? !northeast.equals(envelope.northeast) : envelope.northeast != null) return false;
    if (southwest != null ? !southwest.equals(envelope.southwest) : envelope.southwest != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = southwest != null ? southwest.hashCode() : 0;
    result = 31 * result + (northeast != null ? northeast.hashCode() : 0);
    return result;
  }

  public Coordinate getSouthwest() {
    return southwest;
  }

  public void setSouthwest(Coordinate southwest) {
    this.southwest = southwest;
  }

  public Coordinate getNortheast() {
    return northeast;
  }

  public void setNortheast(Coordinate northeast) {
    this.northeast = northeast;
  }
}
