package se.kodapan.osm.parser.xml.streaming;

import java.io.Reader;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;

/**
 * Created by kalle on 10/16/13.
 */
public abstract  class StreamingOsmXmlParser {

  public static Class<StreamingOsmXmlParser> factoryClass;

  public static StreamingOsmXmlParser newInstance() {
    synchronized (StreamingOsmXmlParser.class) {
      if (factoryClass == null) {
        try {
          factoryClass = (Class<StreamingOsmXmlParser>) Class.forName(StreamingOsmXmlParser.class.getName() + "Impl");
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    }
    try {
      return factoryClass.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

    public abstract void read(Reader xml, StreamingOsmXmlParserListener listener) throws Exception;



}