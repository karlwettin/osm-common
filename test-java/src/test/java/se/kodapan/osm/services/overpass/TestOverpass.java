package se.kodapan.osm.services.overpass;

import se.kodapan.osm.OsmCommonTest;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.PojoRoot;
import se.kodapan.osm.parser.xml.instantiated.InstantiatedOsmXmlParser;

import java.io.StringReader;

/**
 * @author kalle
 * @since 2013-05-04 16:09
 */
public class TestOverpass extends OsmCommonTest {


  public void test() throws Exception {

    Overpass overpass = new Overpass();
    setUserAgent(overpass);
    overpass.open();


    PojoRoot root = new PojoRoot();
    InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
    parser.setRoot(root);

    parser.parse(new StringReader(
        overpass.execute("<osm-script>\n" +
            "  <query type=\"node\">\n" +
            "    <has-kv k=\"ref:se:pts:postort\" v=\"LÖDERUP\"/>\n" +
            "  </query>\n" +
            "  <print/>\n" +
            "</osm-script>")));

    OverpassUtils overpassUtils = new OverpassUtils(overpass);

    overpassUtils.loadAllObjects(root);


    Node halmstad = overpassUtils.getNode(1594669682l);
    Way magnusStenbocksVäg = overpassUtils.loadWay(43153974l);
    Relation sweden = overpassUtils.loadRelation(52822l);


  }

}
