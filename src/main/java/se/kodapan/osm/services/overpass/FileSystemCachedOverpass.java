package se.kodapan.osm.services.overpass;

import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author kalle
 * @since 2013-07-28 00:00
 */
public class FileSystemCachedOverpass extends AbstractCachedOverpass {

  private File path;

  public FileSystemCachedOverpass(File path) {
    this.path = path;
  }

  @Override
  public void open() throws Exception {
    super.open();
    if (!path.exists() && !path.mkdirs()) {
      throw new IOException("Could not mkdirs " + path.getAbsolutePath());
    }
  }

  @Override
  public String getCachedResponse(String overpassQuery) throws Exception {
    String filename = getFileName(overpassQuery);
    File file = new File(path, filename);
    if (!file.exists()) {
      return null;
    }
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
    Record record = (Record) ois.readObject();
    ois.close();

    return record.getOverpassResponse();
  }

  @Override
  public void setCachedResponse(String overpassQuery, String overpassResponse) throws Exception {

    String filename = getFileName(overpassQuery);
    File file = new File(path, filename);

    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
    oos.writeObject(new Record(overpassQuery, overpassResponse, System.currentTimeMillis()));
    oos.close();


  }

  private String getFileName(String overpassQuery) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    return Hex.encodeHexString(md.digest(overpassQuery.getBytes()));
  }

  public static class Record implements Serializable {
    private static final long serialVersionUID = 1l;
    private String overpassQuery;
    private String overpassResponse;
    private long timestamp;


    public Record() {
    }

    public Record(String overpassQuery, String overpassResponse, long timestamp) {
      this.overpassQuery = overpassQuery;
      this.overpassResponse = overpassResponse;
      this.timestamp = timestamp;
    }

    public String getOverpassResponse() {
      return overpassResponse;
    }

    public void setOverpassResponse(String overpassResponse) {
      this.overpassResponse = overpassResponse;
    }

    public String getOverpassQuery() {
      return overpassQuery;
    }

    public void setOverpassQuery(String overpassQuery) {
      this.overpassQuery = overpassQuery;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }
  }
}
