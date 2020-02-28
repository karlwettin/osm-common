package se.kodapan.osm.util.slippymap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Same as TMS, but no inverted Y axis.
 * Used by OpenStreetMap.
 *
 * @author Karl Wettin <mailto:karl.wettin@kodapan.se>
 * @since 2018-11-28
 */
public class XYZ extends SlippyMap {

  private static Logger log = LoggerFactory.getLogger(XYZ.class);

  public XYZ() {
  }

  public XYZ(String urlPattern) {
    super(urlPattern);
  }


  @Override
  public void listTiles(double southLatitude, double westLongitude, double northLatitude, double eastLongitude, int z, TileVisitor tileVisitor) {
    Tile southWest = tileFactory(westLongitude, southLatitude, z);
    Tile northEast = tileFactory(eastLongitude, northLatitude, z);
    Tile tile = new XYZTile(0, 0, z);
    for (int x = southWest.getX(); x <= northEast.getX(); x++) {
      for (int y = northEast.getY(); y <= southWest.getY(); y++) {
        tile.setX(x);
        tile.setY(y);
        tileVisitor.visit(tile);
      }
    }
  }

  @Override
  public Tile tileFactory(double longitude, double latitude, int z) {
    int x = (int) Math.floor((longitude + 180) / 360 * (1 << z));
    int y = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(latitude)) + 1 / Math.cos(Math.toRadians(latitude))) / Math.PI) / 2 * (1 << z));
    return new XYZTile(x, y, z);
  }

  @Override
  public XYZTile tileFactory(int x, int y, int z) {
    return new XYZTile(x, y, z);
  }

  public static class XYZTile extends Tile {

    public XYZTile(int x, int y, int z) {
      super(x, y, z);
    }

    public double evaluateLongitude(int x, int z) {
      return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public double evaluateLatitude(int y, int z) {
      double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
      return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    @Override
    public double getNorthLatitude() {
      return evaluateLatitude(getY(), getZ());
    }

    @Override
    public double getSouthLatitude() {
      return evaluateLatitude(getY() - 1, getZ());
    }

    @Override
    public double getEastLongitude() {
      return evaluateLongitude(getX() + 1, getZ());
    }

    @Override
    public double getWestLongitude() {
      return evaluateLongitude(getX(), getZ());
    }

  }


}
