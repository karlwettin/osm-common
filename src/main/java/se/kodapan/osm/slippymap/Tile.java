package se.kodapan.osm.slippymap;

/**
 * @author kalle
 * @since 8/25/13 7:14 PM
 */
public class Tile {

  private int x, y, z;


  public Tile(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }



  private double evaluateLongitude(int x, int z) {
    return x / Math.pow(2.0, z) * 360.0 - 180;
  }

  private double evaluateLatitude(int y, int z) {
    double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }

  public double getNorthLatitude() {
    return evaluateLatitude(y, z);
  }

  public double getSouthLatitude() {
    return evaluateLatitude(y + 1, z);
  }

  public double getEastLongitude() {
    return evaluateLongitude(x + 1, z);
  }

  public double getWestLongitude() {
    return evaluateLongitude(x, z);
  }

  public double getCentroidLongitude() {
    return (getWestLongitude() + getEastLongitude()) / 2d;
  }

  public double getCentroidLatitude() {
    return (getSouthLatitude() + getNorthLatitude()) / 2d;
  }


  @Override
  public String toString() {
    return "Tile{" +
        "x=" + x +
        ", y=" + y +
        ", z=" + z +
        '}';
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getZ() {
    return z;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Tile tile = (Tile) o;

    if (x != tile.x) return false;
    if (y != tile.y) return false;
    if (z != tile.z) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    result = 31 * result + z;
    return result;
  }
}
