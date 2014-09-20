package se.kodapan.osm.parser.xml;

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

  private static final String format1 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static final String format2 = "yyyy-MM-dd'T'HH:mm:ss";

  private DateFormat implementation1;
  private DateFormat implementation2;

  public OsmXmlTimestampFormat() {
    implementation1 = new SimpleDateFormat(format1);
    implementation2 = new SimpleDateFormat(format2);
  }

  @Override
  public StringBuffer format(Date date, StringBuffer stringBuffer, FieldPosition fieldPosition) {
    return implementation1.format(date, stringBuffer, fieldPosition);
  }

  @Override
  public Date parse(String s, ParsePosition parsePosition) {
    if (s.length() - parsePosition.getIndex() == format1.length()) {
      return implementation1.parse(s, parsePosition);
    }
    return implementation2.parse(s, parsePosition);
  }
}
