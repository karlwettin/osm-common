package se.kodapan.geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A GeoJSON object with the type "FeatureCollection" is a feature collection object.
 * <p/>
 * An object of type "FeatureCollection" must have a member with the name "features". The value corresponding to "features" is an array. Each element in the array is a feature object as defined above.
 *
 * @author kalle
 * @since 2014-09-21 12:53
 */
public class FeatureCollection extends GeoJSONObject {

  private List<Feature> features = new ArrayList<Feature>();

  @Override
  public void writeJSON(Writer writer) throws IOException {
    writer.write("{");
    writer.write("\"type\":\"FeatureCollection\",\"features\":[");
    for (Iterator<Feature> features = getFeatures().iterator(); features.hasNext();) {
      features.next().writeJSON(writer);
      if (features.hasNext()) {
        writer.write(",");
      }
    }
    writer.write("]}");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FeatureCollection that = (FeatureCollection) o;

    if (features != null ? !features.equals(that.features) : that.features != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return features != null ? features.hashCode() : 0;
  }

  public List<Feature> getFeatures() {
    return features;
  }

  public void setFeatures(List<Feature> features) {
    this.features = features;
  }
}
