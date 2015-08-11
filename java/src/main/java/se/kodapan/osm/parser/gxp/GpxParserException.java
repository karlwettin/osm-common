package se.kodapan.osm.parser.gxp;

/**
 * @author kalle
 * @since 2015-08-11 19:12
 */
public class GpxParserException extends Exception {

  public GpxParserException() {
  }

  public GpxParserException(String s) {
    super(s);
  }

  public GpxParserException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public GpxParserException(Throwable throwable) {
    super(throwable);
  }
}

