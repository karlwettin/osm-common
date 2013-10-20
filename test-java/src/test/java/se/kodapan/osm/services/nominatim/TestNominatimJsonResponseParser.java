package se.kodapan.osm.services.nominatim;

import se.kodapan.osm.OsmCommonTest;
import se.kodapan.osm.domain.root.PojoRoot;
import se.kodapan.osm.services.nominatim.NominatimJsonResponseParser.Result;
import se.kodapan.osm.services.overpass.Overpass;
import se.kodapan.osm.services.overpass.OverpassUtils;

import java.util.List;

/**
 * @author kalle
 * @since 2013-07-27 21:15
 */
public class TestNominatimJsonResponseParser extends OsmCommonTest {

  public void test() throws Exception {

    NominatimQueryBuilder queryBuilder = new NominatimQueryBuilder()
            .setQuery("Halmstad, Sverige")
            .setLimit(10)
            .setFormat("json")
            .addCountryCode("se");

    Nominatim nominatim = new Nominatim();
    setUserAgent(nominatim);
    nominatim.open();

    PojoRoot root = new PojoRoot();
    NominatimJsonResponseParser parser = new NominatimJsonResponseParser();
    parser.setRoot(root);
    List<Result> results = parser.parse(nominatim.search(queryBuilder.build()));

    assertTrue(results.size() > 2);
    assertEquals(root.gatherAllOsmObjects().size(), results.size());

    results = parser.parse(nominatim.search(queryBuilder.setLimit(1).build()));
    assertEquals(1, results.size());

//    Overpass overpass = new Overpass();
//    setUserAgent(overpass);
//    overpass.open();
//
//    OverpassUtils overpassUtils = new OverpassUtils(overpass);
//
//    overpassUtils.loadAllObjects(parser.getRoot());

  }

}
