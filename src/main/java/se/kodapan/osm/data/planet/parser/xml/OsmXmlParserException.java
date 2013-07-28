package se.kodapan.osm.data.planet.parser.xml;

/**
 * @author kalle
 * @since 2013-05-01 21:39
 */
public class OsmXmlParserException extends Exception {

  public OsmXmlParserException() {
  }

  public OsmXmlParserException(String s) {
    super(s);
  }

  public OsmXmlParserException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public OsmXmlParserException(Throwable throwable) {
    super(throwable);
  }
}
