package se.kodapan.osm.services.nominatim;

import se.kodapan.util.Hex;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author kalle
 * @since 2013-07-28 19:59
 */
public class FileSystemCachedNominatim extends AbstractCachedNominatim {

  private File path;

  public FileSystemCachedNominatim(File path) {
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
  public String getCachedResponse(String nominatimQuery) throws Exception {
    String filename = getFileName(nominatimQuery);
    File file = new File(path, filename);
    if (!file.exists()) {
      return null;
    }
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
    Record record = (Record) ois.readObject();
    ois.close();

    return record.getNominatimResponse();
  }

  @Override
  public void setCachedResponse(String nominatimQuery, String nominatimResponse) throws Exception {

    String filename = getFileName(nominatimQuery);
    File file = new File(path, filename);

    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
    oos.writeObject(new Record(nominatimQuery, nominatimResponse, System.currentTimeMillis()));
    oos.close();


  }

  private String getFileName(String nominatimQuery) throws NoSuchAlgorithmException, UnsupportedEncodingException {
      return Hex.encodeHexString(MessageDigest.getInstance("SHA-1").digest(nominatimQuery.getBytes("utf8")));
  }

  public static class Record implements Serializable {
    private static final long serialVersionUID = 1l;
    private String url;
    private String nominatimResponse;
    private long timestamp;


    public Record() {
    }

    public Record(String url, String nominatimResponse, long timestamp) {
      this.url = url;
      this.nominatimResponse = nominatimResponse;
      this.timestamp = timestamp;
    }

    public String getNominatimResponse() {
      return nominatimResponse;
    }

    public void setNominatimResponse(String nominatimResponse) {
      this.nominatimResponse = nominatimResponse;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }
  }


}
