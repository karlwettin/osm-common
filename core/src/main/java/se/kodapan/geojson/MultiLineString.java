package se.kodapan.geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Coordinates of a MultiLineString are an array of LineString coordinate arrays:
 * <p/>
 * <pre>
 * {
 *   "type": "MultiLineString",
 *   "coordinates": [
 *     [ [100.0, 0.0], [101.0, 1.0] ],
 *     [ [102.0, 2.0], [103.0, 3.0] ]
 *   ]
 * }
 *</pre>
 *
 * @author kalle
 * @since 2014-09-21 12:49
 */
public class MultiLineString extends GeoJSONGeometry {

  private List<LineString> lineStrings = new ArrayList<LineString>();

  @Override
  public void writeJSON(Writer writer) throws IOException {
    writer.write("{");
    writer.write("\"type\":\"\",\"coordinates\":[");
    for (Iterator<LineString> lineStrings = getLineStrings().iterator(); lineStrings.hasNext();) {
      lineStrings.next().writeLineStringPart(writer);
      if (lineStrings.hasNext()) {
        writer.write(",");
      }
    }
    writer.write("]}");
  }

  public List<LineString> getLineStrings() {
    return lineStrings;
  }

  public void setLineStrings(List<LineString> lineStrings) {
    this.lineStrings = lineStrings;
  }
}
