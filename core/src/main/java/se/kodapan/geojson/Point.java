package se.kodapan.geojson;

import java.io.IOException;
import java.io.Writer;

/**
 * Point coordinates are in x, y order (easting, northing for projected coordinates, longitude, latitude for geographic coordinates):
 * <pre>
 * {
 *   "type": "Point",
 *   "coordinates": [100.0, 0.0]
 * }
 * </pre>
 *
 * @author kalle
 * @since 2014-09-21 12:52
 */
public class Point extends GeoJSONGeometry {

  private double latitude;
  private double longitude;

  public Point() {
  }

  public Point(double longitude, double latitude) {
    this.longitude = longitude;
    this.latitude = latitude;
  }

  @Override
  public void writeJSON(Writer writer) throws IOException {
    writer.write("{");
    writer.write("\"type\":\"Point\",\"coordinates\":");
    writeCoordinatePart(writer);
    writer.write("}");
  }

  public void writeCoordinatePart(Writer writer) throws IOException {
    writer.write("[");
    writer.write(String.valueOf(getLongitude()));
    writer.write(",");
    writer.write(String.valueOf(getLatitude()));
    writer.write("]");
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }
}
