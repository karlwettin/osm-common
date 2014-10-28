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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point point = (Point) o;

    if (Double.compare(point.latitude, latitude) != 0) return false;
    if (Double.compare(point.longitude, longitude) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(latitude);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
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
