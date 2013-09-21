package se.kodapan.osm.util.slippymap;

/**
 * @author kalle
 * @since 8/25/13 7:14 PM
 */
public abstract class Tile {

  private int x, y, z;


  protected Tile(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public abstract double getNorthLatitude();

  public abstract double getSouthLatitude();

  public abstract double getEastLongitude();

  public abstract double getWestLongitude();

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
