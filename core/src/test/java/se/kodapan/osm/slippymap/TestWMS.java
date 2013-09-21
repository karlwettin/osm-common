package se.kodapan.osm.slippymap;

/**
 * @author kalle
 * @since 2013-09-21 4:45 PM
 */
public class TestWMS extends SlippyMapTest {

  @Override
  protected SlippyMap mapFactory() {
    return new WMS();
  }
}
