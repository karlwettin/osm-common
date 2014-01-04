package se.kodapan.osm.util;

import java.io.Serializable;

/**
 * @author kalle
 * @since 2013-11-03 23:25
 */
public class Coordinate implements Serializable {

  private static final long serialVersionUID = 1l;

  private double latitude;
  private double longitude;

  public Coordinate() {
  }

  public Coordinate(Coordinate coordinate) {
    this(coordinate.getLatitude(), coordinate.getLongitude());
  }

  public Coordinate(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Coordinate that = (Coordinate) o;

    if (Double.compare(that.latitude, latitude) != 0) return false;
    if (Double.compare(that.longitude, longitude) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(latitude);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }
}
