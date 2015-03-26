package se.kodapan.osm.city;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kalle
 * @since 2015-01-12 04:36
 */
public class Color {

  private byte attributeIndex;

  private int rgb;

  private int red;
  private int green;
  private int blue;


  public byte getAttributeIndex() {
    return attributeIndex;
  }

  public void setAttributeIndex(byte attributeIndex) {
    this.attributeIndex = attributeIndex;
  }

  public int getRgb() {
    return rgb;
  }

  public void setRgb(int rgb) {
    java.awt.Color color = new java.awt.Color(rgb);
    this.rgb = rgb;
    this.red = color.getRed();
    this.green = color.getGreen();
    this.blue = color.getBlue();
  }

  public int getRed() {
    return red;
  }

  public int getGreen() {
    return green;
  }

  public int getBlue() {
    return blue;
  }

  public double distanceTo(Color color) {
    double deltaR = getRed() - color.getRed();
    double deltaG = getGreen() - color.getGreen();
    double deltaB = getBlue() - color.getBlue();
    return Math.sqrt(deltaR * deltaR + deltaG * deltaG + deltaB * deltaB);
  }
}
