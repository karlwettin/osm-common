package se.kodapan.geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Coordinates of LineString are an array of positions (see 2.1.1. Positions):
 * <p/>
 * <pre>
 * {
 *   "type": "LineString",
 *   "coordinates": [ [100.0, 0.0], [101.0, 1.0] ]
 * }
 * </pre>
 *
 * @author kalle
 * @since 2014-09-21 12:52
 */
public class LineString extends GeoJSONGeometry {

  private List<Point> coordinates = new ArrayList<Point>();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LineString that = (LineString) o;

    if (coordinates != null ? !coordinates.equals(that.coordinates) : that.coordinates != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return coordinates != null ? coordinates.hashCode() : 0;
  }

  @Override
  public void writeJSON(Writer writer) throws IOException {
    writer.write("{");
    writer.write("\"type\":\"LineString\",\"coordinates\":");
    writeLineStringPart(writer);
    writer.write("}");
  }

  public void writeLineStringPart(Writer writer) throws IOException {
    writer.write("[");
    for (Iterator<Point> coordinates = getCoordinates().iterator(); coordinates.hasNext();) {
      coordinates.next().writeCoordinatePart(writer);
      if (coordinates.hasNext()) {
        writer.write(",");
      }
    }
    writer.write("]");
  }

  public List<Point> getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(List<Point> coordinates) {
    this.coordinates = coordinates;
  }
}
