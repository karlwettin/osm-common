package se.kodapan.geojson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;

/**
 * A GeoJSON object with the type "Feature" is a feature object.
 * <p/>
 * A feature object must have a member with the name "geometry". The value of the geometry member is a geometry object as defined above or a JSON null value.
 * <p/>
 * A feature object must have a member with the name "properties". The value of the properties member is an object (any JSON object or a JSON null value).
 * <p/>
 * If a feature has a commonly used identifier, that identifier should be included as a member of the feature object with the name "id".
 *
 * @author kalle
 * @since 2014-09-21 12:54
 */
public class Feature extends GeoJSONObject {

  private GeoJSONGeometry geometry;
  private JSONObject properties;

  @Override
  public void writeJSON(Writer writer) throws IOException {
    writer.write("{\"type\":\"Feature\",");
    if (getProperties() != null) {
      writer.write("\"properties\":");
      writer.write(getProperties().toString());
      writer.write(",");
    }
    writer.write("\"geometry\":");
    getGeometry().writeJSON(writer);
    writer.write("}");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Feature feature = (Feature) o;

    if (geometry != null ? !geometry.equals(feature.geometry) : feature.geometry != null) return false;
    if (properties != null ? !properties.equals(feature.properties) : feature.properties != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = geometry != null ? geometry.hashCode() : 0;
    result = 31 * result + (properties != null ? properties.hashCode() : 0);
    return result;
  }

  public GeoJSONGeometry getGeometry() {
    return geometry;
  }

  public void setGeometry(GeoJSONGeometry geometry) {
    this.geometry = geometry;
  }

  public JSONObject getProperties() {
    return properties;
  }

  public void setProperties(JSONObject properties) {
    this.properties = properties;
  }
}
