package se.kodapan.osm.city;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kalle
 * @since 2015-01-12 04:46
 */
public class Instance {

  private double south;
  private double west;
  private double north;
  private double east;

  private byte[][] data;

  private double[] histogramPercent;


  public void setData(byte[][] data) {
    this.data = data;
  }

  public double[] getHistogramPercent() {
    return histogramPercent;
  }

  public void setHistogramPercent(double[] histogramPercent) {
    this.histogramPercent = histogramPercent;
  }

  public byte[][] getData() {
    return data;
  }

  public double getSouth() {
    return south;
  }

  public void setSouth(double south) {
    this.south = south;
  }

  public double getWest() {
    return west;
  }

  public void setWest(double west) {
    this.west = west;
  }

  public double getNorth() {
    return north;
  }

  public void setNorth(double north) {
    this.north = north;
  }

  public double getEast() {
    return east;
  }

  public void setEast(double east) {
    this.east = east;
  }

}
