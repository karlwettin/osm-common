package se.kodapan.geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Coordinates of a MultiPolygon are an array of Polygon coordinate arrays:
 * <p/>
 * <pre>
 * {
 *   "type": "MultiPolygon",
 *   "coordinates": [
 *     [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],
 *     [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],
 *     [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]
 *   ]
 * }
 * </pre>
 *
 * @author kalle
 * @since 2014-09-21 12:49
 */
public class MultiPolygon extends GeoJSONGeometry {

  private List<Polygon> polygons = new ArrayList<Polygon>();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MultiPolygon that = (MultiPolygon) o;

    if (polygons != null ? !polygons.equals(that.polygons) : that.polygons != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return polygons != null ? polygons.hashCode() : 0;
  }

  @Override
  public void writeJSON(Writer writer) throws IOException {
    writer.write("{");
    writer.write("\"type\":\"MultiPolygon\",\"coordinates\":[");
    for (Iterator<Polygon> polygons = getPolygons().iterator(); polygons.hasNext(); ) {
      polygons.next().writePolygonPart(writer);
      if (polygons.hasNext()) {
        writer.write(",");
      }
    }
    writer.write("]}");
  }

  public List<Polygon> getPolygons() {
    return polygons;
  }

  public void setPolygons(List<Polygon> polygons) {
    this.polygons = polygons;
  }
}
