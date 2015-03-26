package se.kodapan.osm.city;

import org.apache.commons.codec.binary.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2015-01-12 04:39
 */
public class Palette {

  private List<Color> colors = new ArrayList<Color>();

  public Palette addColor(String rgb) {
    if (rgb.startsWith("#")) {
      rgb = rgb.substring(1);
    }
    int r = Integer.parseInt(rgb.substring(0, 2), 16);
    int g = Integer.parseInt(rgb.substring(2, 4), 16);
    int b = Integer.parseInt(rgb.substring(4, 6), 16);
    return addColor(new java.awt.Color(r, g, b).getRGB());
  }

  public Palette addColor(int rgb) {
    Color color = new Color();
    color.setAttributeIndex((byte)colors.size());
    color.setRgb(rgb);
    colors.add(color);
    return this;
  }

  public Color getColor(int rgb) {
    Color rgbColor = new Color();
    rgbColor.setRgb(rgb);
    double closesDistance = Double.MAX_VALUE;
    Color closestColor = null;
    for (Color color : colors) {
      if (color.getRgb() == rgb) {
        return color;
      }
      double distance = color.distanceTo(rgbColor);
      if (distance < closesDistance) {
        closestColor = color;
        closesDistance = distance;
      }
    }
    return closestColor;
  }

  public List<Color> getColors() {
    return colors;
  }

  public void setColors(List<Color> colors) {
    this.colors = colors;
  }
}
