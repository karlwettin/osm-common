package se.kodapan.osm.util.slippymap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tool for evaluating Slippy map tile numbers, URLs, etc.
 *
 * @author kalle
 * @since 8/25/13 1:29 PM
 */
public abstract class SlippyMap {

  private String urlPattern = null;

  protected SlippyMap() {
  }

  protected SlippyMap(String urlPattern) {
    this.urlPattern = urlPattern;
  }

  public abstract Tile tileFactory(double longitude, double latitude, int z);
  public abstract Tile tileFactory(int x, int y, int z);

  public abstract  void listTiles(double southLatitude, double westLongitude, double northLatitude, double eastLongitude, int z, TileVisitor tileVisitor);

  @Deprecated
  public List<Tile> listTiles(double southLatitude, double westLongitude, double northLatitude, double eastLongitude, int z) {
    final List<Tile> tiles = new ArrayList<Tile>(1000);
    listTiles(southLatitude, westLongitude, northLatitude, eastLongitude, z, new TileVisitor() {
      @Override
      public void visit(Tile tile) {
        tiles.add(new WMS.WMSTile(tile.getX(), tile.getY(), tile.getZ()));
      }
    });
    return tiles;
  }

  @Deprecated
  public Iterator<Tile> iterateTiles(double southLatitude, double westLongitude, double northLatitude, double eastLongitude, int z) {
    return listTiles(southLatitude, westLongitude, northLatitude, eastLongitude, z).iterator();
  }

  public String toURL(Tile tile) {
    return urlPattern
        .replaceAll("\\%\\{x\\}", String.valueOf(tile.getX()))
        .replaceAll("\\%\\{y\\}", String.valueOf(tile.getY()))
        .replaceAll("\\%\\{z\\}", String.valueOf(tile.getZ()));
  }

  public String getUrlPattern() {
    return urlPattern;
  }

  public void setUrlPattern(String urlPattern) {
    this.urlPattern = urlPattern;
  }
}
