package se.kodapan.geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Coordinates of a Polygon are an array of LinearRing coordinate arrays. The first element in the array represents the exterior ring. Any subsequent elements represent interior rings (or holes).
 * <p/>
 * No holes:
 * <p/>
 * <pre>
 * {
 *   "type": "Polygon",
 *   "coordinates": [
 *     [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
 *   ]
 * }
 * </pre>
 * <p/>
 * With holes:
 * <p/>
 * <pre>
 * {
 *   "type": "Polygon",
 *   "coordinates": [
 *     [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ],
 *     [ [100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ]
 *   ]
 * }
 * </pre>
 *
 * @author kalle
 * @since 2014-09-21 12:50
 */
public class Polygon extends GeoJSONGeometry {

  private List<Point> hull = new ArrayList<Point>();
  private List<List<Point>> holes = new ArrayList<List<Point>>();

  @Override
  public void writeJSON(Writer writer) throws IOException {
    writer.write("{");
    writer.write("\"type\":\"Polygon\",\"coordinates\":");

    writePolygonPart(writer);

    writer.write("}");

  }

  public void writePolygonPart(Writer writer) throws IOException {
    writer.write("[");

    writer.write("[");
    for (Iterator<Point> coordinates = getHull().iterator(); coordinates.hasNext(); ) {
      coordinates.next().writeCoordinatePart(writer);
      if (coordinates.hasNext()) {
        writer.write(",");
      }
    }
    writer.write("]");

    if (getHoles() != null && !getHoles().isEmpty()) {
      for (Iterator<List<Point>> holes = getHoles().iterator(); holes.hasNext(); ) {
        List<Point> hole = holes.next();
        writer.write(",");
        writer.write("[");
        for (Iterator<Point> coordinates = hole.iterator(); coordinates.hasNext(); ) {
          coordinates.next().writeCoordinatePart(writer);
          if (coordinates.hasNext()) {
            writer.write(",");
          }
        }

        writer.write("]");
      }
    }


    writer.write("]");
  }

  public List<Point> getHull() {
    return hull;
  }

  public void setHull(List<Point> hull) {
    this.hull = hull;
  }

  public List<List<Point>> getHoles() {
    return holes;
  }

  public void setHoles(List<List<Point>> holes) {
    this.holes = holes;
  }
}
