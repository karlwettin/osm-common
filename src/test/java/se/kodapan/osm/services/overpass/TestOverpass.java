package se.kodapan.osm.services.overpass;

import se.kodapan.osm.OsmCommonTest;
import se.kodapan.osm.data.planet.parser.xml.instantiated.InstantiatedOsmXmlParser;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.Root;

import java.io.StringReader;

/**
 * @author kalle
 * @since 2013-05-04 16:09
 */
public class TestOverpass extends OsmCommonTest {


  public void test() throws Exception {

    Overpass overpass = new Overpass();
    setUserAgent(overpass);



    Root root = new Root();
    InstantiatedOsmXmlParser parser = new InstantiatedOsmXmlParser();
    parser.setRoot(root);

    parser.parse(new StringReader(
        overpass.execute("<osm-script>\n" +
            "  <query type=\"node\">\n" +
            "    <has-kv k=\"ref:se:pts:postort\" v=\"LÖDERUP\"/>\n" +
            "  </query>\n" +
            "  <print/>\n" +
            "</osm-script>")));

    overpass.loadAllObjects(root);


    Node halmstad = overpass.getNode(1594669682l);
    Way magnusStenbocksVäg = overpass.loadWay(43153974l);
    Relation sweden = overpass.loadRelation(52822l);

    System.currentTimeMillis();

  }

}
