package se.kodapan.geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Each element in the geometries array of a GeometryCollection is one of the geometry objects described above:
 * <p/>
 * <pre>
 * {
 *   "type": "GeometryCollection",
 *   "geometries": [ {
 *       "type": "Point",
 *       "coordinates": [100.0, 0.0]
 *     }, {
 *       "type": "LineString",
 *       "coordinates": [ [101.0, 0.0], [102.0, 1.0] ]
 *     }
 *   ]
 * }
 * </pre>
 *
 * @author kalle
 * @since 2014-09-21 12:47
 */
public class GeometryCollection extends GeoJSONGeometry {

  private List<GeoJSONGeometry> geometries = new ArrayList<GeoJSONGeometry>();

  @Override
  public void writeJSON(Writer writer) throws IOException {
    writer.write("{");
    writer.write("\"type\":\"GeometryCollection\",\"geometries\":[");
    for (Iterator<GeoJSONGeometry> geometries = getGeometries().iterator(); geometries.hasNext();) {
      geometries.next().writeJSON(writer);
      if (geometries.hasNext()) {
        writer.write(",");
      }
    }
    writer.write("]}");
  }

  public List<GeoJSONGeometry> getGeometries() {
    return geometries;
  }

  public void setGeometries(List<GeoJSONGeometry> geometries) {
    this.geometries = geometries;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GeometryCollection that = (GeometryCollection) o;

    if (geometries != null ? !geometries.equals(that.geometries) : that.geometries != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return geometries != null ? geometries.hashCode() : 0;
  }
}
