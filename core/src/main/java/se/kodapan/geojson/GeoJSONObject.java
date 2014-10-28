package se.kodapan.geojson;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author kalle
 * @since 2014-09-21 12:56
 */
public abstract class GeoJSONObject {

  public String toJSON() {
    StringWriter buf = new StringWriter(4096);
    try {
      writeJSON(buf);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return buf.toString();
  }

  public abstract void writeJSON(Writer writer) throws IOException;

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();
}
