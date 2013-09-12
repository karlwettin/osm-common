package se.kodapan.osm.jts;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.geo.distance.ArcDistance;

import java.io.Serializable;

/**
 * @author kalle
 * @since 2013-09-05 12:40 AM
 */
public class EnvelopeGrid {

  private double metersDiagonal;
  private double centroidLatitude;
  private double centroidLongitude;

  private Envelope[][] grid;

  public EnvelopeGrid(double metersDiagonal, double centroidLatitude, double centroidLongitude, int rows, int columns) {
    this.metersDiagonal = metersDiagonal;
    this.centroidLatitude = centroidLatitude;
    this.centroidLongitude = centroidLongitude;

    double s = centroidLatitude;
    double n = centroidLatitude;
    double w = centroidLongitude;
    double e = centroidLongitude;

    {
      double diagonalKiloMetersSearchArea = 0.001 * metersDiagonal;

      ArcDistance distance = new ArcDistance();
      double increment = 0.00001;
      while (distance.calculate(s, centroidLongitude, n, centroidLongitude) < diagonalKiloMetersSearchArea) {
        s -= increment;
        n += increment;
      }
      while (distance.calculate(centroidLatitude, w, centroidLatitude, e) < diagonalKiloMetersSearchArea) {
        w -= increment;
        e += increment;
      }
    }


    Envelope envelope = new Envelope(s, w, n, e);

    grid = new Envelope[rows][];

    double rowIncrement = (n - s) / (double) rows;
    double columnIncrement = (e - w) / (double) columns;

    double swCornerLongitude = envelope.westLongitude;
    for (int row = 0; row < rows; row++) {
      grid[row] = new Envelope[columns];
      double swCornerLatitude = envelope.southLatitude;
      for (int column = 0; column < columns; column++) {
        grid[row][column] = new Envelope(swCornerLatitude, swCornerLongitude, swCornerLatitude + rowIncrement, swCornerLongitude + columnIncrement);
        swCornerLatitude += rowIncrement;
      }
      swCornerLongitude += columnIncrement;
    }

  }

  public static class Envelope implements Serializable {

    private static final long serialVersionUID = 1l;

    private double southLatitude;
    private double northLatitude;
    private double westLongitude;
    private double eastLongitude;

    public Envelope(double southLatitude, double westLongitude, double northLatitude, double eastLongitude) {
      this.southLatitude = southLatitude;
      this.westLongitude = westLongitude;
      this.northLatitude = northLatitude;
      this.eastLongitude = eastLongitude;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Envelope envelope = (Envelope) o;

      if (Double.compare(envelope.eastLongitude, eastLongitude) != 0) return false;
      if (Double.compare(envelope.northLatitude, northLatitude) != 0) return false;
      if (Double.compare(envelope.southLatitude, southLatitude) != 0) return false;
      if (Double.compare(envelope.westLongitude, westLongitude) != 0) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result;
      long temp;
      temp = Double.doubleToLongBits(southLatitude);
      result = (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(northLatitude);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(westLongitude);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(eastLongitude);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      return result;
    }

    @Override
    public String toString() {
      return "Envelope{" +
          "southLatitude=" + southLatitude +
          ", northLatitude=" + northLatitude +
          ", westLongitude=" + westLongitude +
          ", eastLongitude=" + eastLongitude +
          '}';
    }

    public double getCentroidLatitude() {
      return (getNorthLatitude() + getSouthLatitude()) / 2;
    }

    public double getCentroidLongitude() {
      return (getEastLongitude() + getWestLongitude()) / 2;
    }

    public boolean contains(Node node) {
      return contains(node.getLatitude(), node.getLongitude());
    }

    public boolean contains(double latitude, double longitude) {
      return southLatitude >= latitude && northLatitude <= latitude && westLongitude >= longitude && eastLongitude <= longitude;
    }

    public double getSouthLatitude() {
      return southLatitude;
    }

    public double getNorthLatitude() {
      return northLatitude;
    }

    public double getWestLongitude() {
      return westLongitude;
    }

    public double getEastLongitude() {
      return eastLongitude;
    }
  }

  public Envelope[][] getGrid() {
    return grid;
  }

  public double getMetersDiagonal() {
    return metersDiagonal;
  }

  public double getCentroidLatitude() {
    return centroidLatitude;
  }

  public double getCentroidLongitude() {
    return centroidLongitude;
  }


  public static boolean[][] shapeFilter(String input) {
    String[] rows = input.split("\n");
    boolean shape[][] = new boolean[rows[0].length()][];
    for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
      String row = rows[rowIndex];
      shape[rowIndex] = new boolean[row.length()];
      char[] charArray = row.toCharArray();
      for (int colIndex = 0; colIndex < charArray.length; colIndex++) {
        char col = charArray[colIndex];
        if (' ' != col) {
          shape[rowIndex][colIndex] = true;
        }
      }
    }
    return shape;
  }

}


