package se.kodapan.osm.services.overpass;

import junit.framework.TestCase;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;

/**
 * @author kalle
 * @since 2013-05-04 16:09
 */
public class TestOverpass extends TestCase {

  public void test() throws Exception {

    Overpass overpass = new Overpass();

    Node halmstad = overpass.getNode(1594669682l);
    Way magnusStenbocksVäg = overpass.getWay(43153974l);
    Relation sweden = overpass.getRelation(52822l);

    System.currentTimeMillis();

  }

}
