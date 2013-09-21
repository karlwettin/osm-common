package se.kodapan.osm.slippymap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tool for evaluating Slippy mat tile numers, URLs, etc.
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

  public Iterator<Tile> iterateTiles(double southLatitude, double westLongitude, double northLatitude, double eastLongitude, int z) {
    // todo for those very large tile iterations. should of course not create a list first...
    return listTiles(southLatitude,westLongitude, northLatitude, eastLongitude, z).iterator();
  }
  public abstract List<Tile> listTiles(double southLatitude, double westLongitude, double northLatitude, double eastLongitude, int z);

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
