package se.kodapan.osm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author kalle
 * @since 2012-01-21 14:22
 */
public class Grid {
  public static Logger log = LoggerFactory.getLogger(Grid.class);
  private double latitudeStep;
  private double longitudeStep;

  private long height;
  private long width;

  public Grid(double cellWidthKilometers) {

    this.width = (long) (6378.1d / cellWidthKilometers);
    this.height = width;

    init();
  }


  public Grid(long width, long height) {
    this.height = height;
    this.width = width;
    init();
  }

  public void init() {
    latitudeStep = 180d / (double) height;
    longitudeStep = 360d / (double) width;
  }

  public class Cell {
    private long top;
    private long left;

    public Cell(long top, long left) {
      this.top = top;
      this.left = left;
    }

    private Coordinate getNorthwest() {
      return new Coordinate(90 - (top * latitudeStep), -180 + (left * longitudeStep));
    }

    public Envelope getEnvelope() {
      Coordinate southwest = getSouthwest();
      Coordinate northeast = getNortheast();
      return new Envelope(southwest, northeast);
    }

    public Coordinate getNortheast() {
      Coordinate northWest = getNorthwest();
      return new Coordinate(getNorthwest().getLatitude(), northWest.getLongitude() + longitudeStep);
    }

    public Coordinate getSouthwest() {
      Coordinate northWest = getNorthwest();
      return new Coordinate(getNorthwest().getLatitude() - latitudeStep, northWest.getLongitude());
    }

    public long getIdentity() {
      return (top * width) + left;
    }

    public long getTop() {
      return top;
    }

    public long getLeft() {
      return left;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Cell cell = (Cell) o;

      if (left != cell.left) return false;
      if (top != cell.top) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = (int) (top ^ (top >>> 32));
      result = 31 * result + (int) (left ^ (left >>> 32));
      return result;
    }
  }

  public Cell getCell(long id) {
    long top = (id / this.width);
    long left = (id - (top * this.height));
    return new Cell(top, left);
  }

  public Cell getCell(Coordinate coordinate) {
    long top = (long) ((90d - coordinate.getLatitude()) / latitudeStep);
    long left = (long) ((coordinate.getLongitude() - -180d) / longitudeStep);
    return new Cell(top, left);
  }


  public long getHeight() {
    return height;
  }

  public long getWidth() {
    return width;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Grid grid = (Grid) o;

    if (height != grid.height) return false;
    if (Double.compare(grid.latitudeStep, latitudeStep) != 0) return false;
    if (Double.compare(grid.longitudeStep, longitudeStep) != 0) return false;
    if (width != grid.width) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = latitudeStep != +0.0d ? Double.doubleToLongBits(latitudeStep) : 0L;
    result = (int) (temp ^ (temp >>> 32));
    temp = longitudeStep != +0.0d ? Double.doubleToLongBits(longitudeStep) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (int) (height ^ (height >>> 32));
    result = 31 * result + (int) (width ^ (width >>> 32));
    return result;
  }

  private class Envelope {
    private Coordinate southwest;
    private Coordinate northeast;

    private Envelope() {
    }

    private Envelope(Coordinate southwest, Coordinate northeast) {
      this.southwest = southwest;
      this.northeast = northeast;
    }

    private Coordinate getSouthwest() {
      return southwest;
    }

    private void setSouthwest(Coordinate southwest) {
      this.southwest = southwest;
    }

    private Coordinate getNortheast() {
      return northeast;
    }

    private void setNortheast(Coordinate northeast) {
      this.northeast = northeast;
    }
  }


}
