package se.kodapan.osm.data.planet.parser;

import junit.framework.TestCase;
import se.kodapan.osm.data.planet.parser.xml.OsmXmlTimestampFormat;

import java.util.Date;

/**
 * @author kalle
 * @since 2013-05-01 16:02
 */
public class TestOsmXmlTimestampFormat extends TestCase {

  public void test() throws Exception {

    OsmXmlTimestampFormat format = new OsmXmlTimestampFormat();
    String string = "2006-12-31T20:57:28Z";
    Date parsed = format.parse(string);
    String formatted = format.format(parsed);
    assertEquals(1167595048000l, parsed.getTime());
    assertEquals(string, formatted);

  }
}
