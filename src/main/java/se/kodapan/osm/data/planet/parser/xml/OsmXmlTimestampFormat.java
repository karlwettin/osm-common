package se.kodapan.osm.data.planet.parser.xml;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author kalle
 * @since 2013-05-01 15:59
 */
public class OsmXmlTimestampFormat extends DateFormat {

  private static final String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  private DateFormat implementation;

  public OsmXmlTimestampFormat() {
    implementation = new SimpleDateFormat(format);
  }

  @Override
  public StringBuffer format(Date date, StringBuffer stringBuffer, FieldPosition fieldPosition) {
    return implementation.format(date, stringBuffer, fieldPosition);
  }

  @Override
  public Date parse(String s, ParsePosition parsePosition) {
    return implementation.parse(s, parsePosition);
  }
}
