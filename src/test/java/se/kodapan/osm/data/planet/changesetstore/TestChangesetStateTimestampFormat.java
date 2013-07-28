package se.kodapan.osm.data.planet.changesetstore;

import junit.framework.TestCase;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author kalle
 * @since 2013-05-01 16:02
 */
public class TestChangesetStateTimestampFormat extends TestCase {

  public void test() throws Exception {

    DateFormat format = new ChangesetStateTimestampFormat();
    String string = "2006-12-31T20\\:57\\:28Z";
    Date parsed = format.parse(string);
    String formatted = format.format(parsed);
    assertEquals(1167595048000l, parsed.getTime());
    assertEquals(string, formatted);

  }
}
