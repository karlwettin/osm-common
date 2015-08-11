package se.kodapan.osm.parser.gxp;

import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author kalle
 * @since 2015-08-11 18:30
 */
public abstract class AbstractInstantiatedGpxParser {

  private static final Logger log = LoggerFactory.getLogger(AbstractInstantiatedGpxParser.class);

  public Stream readerFactory(Reader xml) throws StreamException {
    return readerFactory(new ReaderInputStream(xml, "utf8"));
  }

  public Stream readerFactory(InputStream xml) throws StreamException {
    try {
      return readerFactory(new InputStreamReader(xml, "utf8"));
    } catch (UnsupportedEncodingException e) {
      throw new StreamException(e);
    }
  }

  public static class StreamException extends Exception {
    public StreamException() {
      super();
    }

    public StreamException(String message) {
      super(message);
    }

    public StreamException(String message, Throwable cause) {
      super(message, cause);
    }

    public StreamException(Throwable cause) {
      super(cause);
    }
  }

  public abstract static class Stream {
    public abstract int getEventType() throws StreamException;

    public abstract boolean isEndDocument(int eventType) throws StreamException;

    public abstract int next() throws StreamException;

    public abstract boolean isStartElement(int eventType) throws StreamException;

    public abstract boolean isEndElement(int eventType) throws StreamException;

    public abstract String getLocalName() throws StreamException;

    public abstract String getAttributeValue(String what, String key) throws StreamException;

    public abstract int getAttributeCount() throws StreamException;

    public abstract String getAttributeValue(int index) throws StreamException;

    public abstract String getAttributeLocalName(int index) throws StreamException;

    public abstract String getElementText() throws StreamException;

    public abstract char[] getCharacters() throws StreamException;

    public abstract boolean isCharacters(int eventType) throws StreamException;

    public abstract void close() throws StreamException;
  }


  private XMLInputFactory xmlif = XMLInputFactory.newInstance();
  private XMLStreamReader xmlr;

  public Gpx parse(Reader xml) throws Exception {

    long started = System.currentTimeMillis();

    log.debug("Begin parsing...");


    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    Gpx gpx = new Gpx();

    try {
      Stream xmlr = readerFactory(xml);

      Track currentTrack = null;
      TrackSegment currentTrackSegment = null;
      TrackPoint currentTrackPoint = null;
      WayPoint currentWayPoint = null;

      int eventType = xmlr.getEventType(); // START_DOCUMENT
      while (!xmlr.isEndDocument(eventType = xmlr.next())) {

        if (xmlr.isStartElement(eventType)) {

          if ("wpt".equals(xmlr.getLocalName())) {
            currentWayPoint = new WayPoint();

            for (int attributeIndex = 0; attributeIndex < xmlr.getAttributeCount(); attributeIndex++) {
              String key = xmlr.getAttributeLocalName(attributeIndex);
              String value = xmlr.getAttributeValue(attributeIndex);

              if ("lat".equals(key)) {
                currentWayPoint.setLatitude(Double.valueOf(value));

              } else if ("lon".equals(key)) {
                currentWayPoint.setLongitude(Double.valueOf(value));

              } else {
                throw new GpxParserException("Unknown attribute in way point: " + key + "=" + value);
              }

            }

          } else if ("trk".equals(xmlr.getLocalName())) {
            currentTrack = new Track();

          } else if ("trkseg".equals(xmlr.getLocalName())) {
            currentTrackSegment = new TrackSegment();

          } else if ("trkpt".equals(xmlr.getLocalName())) {
            currentTrackPoint = new TrackPoint();

            for (int attributeIndex = 0; attributeIndex < xmlr.getAttributeCount(); attributeIndex++) {
              String key = xmlr.getAttributeLocalName(attributeIndex);
              String value = xmlr.getAttributeValue(attributeIndex);

              if ("lat".equals(key)) {
                currentTrackPoint.setLatitude(Double.valueOf(value));

              } else if ("lon".equals(key)) {
                currentTrackPoint.setLongitude(Double.valueOf(value));

              } else {
                throw new GpxParserException("Unknown attribute in track point: " + key + "=" + value);
              }

            }

          } else if ("ele".equals(xmlr.getLocalName())) {
            double elevation = Double.valueOf(xmlr.getElementText());
            if (currentTrackPoint != null) {
              currentTrackPoint.setElevation(elevation);
            } else if (currentWayPoint != null) {
              currentWayPoint.setElevation(elevation);
            } else {
              throw new GpxParserException();
            }

          } else if ("time".equals(xmlr.getLocalName())) {
            long timestamp = sdf.parse(xmlr.getElementText()).getTime();
            if (currentTrackPoint != null) {
              currentTrackPoint.setTimestamp(timestamp);
            } else if (currentWayPoint != null) {
              currentWayPoint.setTimestamp(timestamp);
            } else {
              throw new GpxParserException();
            }

          } else {
            log.debug("Skipping start element: " + xmlr.getLocalName());
          }

        } else if (xmlr.isEndElement(eventType)) {

          if ("wpt".equals(xmlr.getLocalName())) {
            gpx.getWayPoints().add(currentWayPoint);
            currentWayPoint = null;

          } else if ("trk".equals(xmlr.getLocalName())) {
            gpx.getTracks().add(currentTrack);
            currentTrack = null;

          } else if ("trkseg".equals(xmlr.getLocalName())) {
            currentTrack.getTrackSegments().add(currentTrackSegment);
            currentTrackSegment = null;

          } else if ("trkpt".equals(xmlr.getLocalName())) {
            currentTrackSegment.getTrackPoints().add(currentTrackPoint);
            currentTrackPoint = null;


          } else {
            log.debug("Skipping end element: " + xmlr.getLocalName());
          }

        }
      }
      xmlr.close();
    } catch (StreamException ioe) {
      throw new GpxParserException(ioe);
    }

    log.debug("Done parsing.");

    long timespent = System.currentTimeMillis() - started;

    log.info("Parsed in " + timespent + " milliseconds.");

    return gpx;
  }


}
