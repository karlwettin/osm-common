package se.kodapan.osm.slippymap;

/**
 * @author kalle
 * @since 2013-09-21 4:46 PM
 */
public class TestTMS extends SlippyMapTest {

  @Override
  protected SlippyMap mapFactory() {
    return new TMS();
  }

}
