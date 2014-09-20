package se.kodapan.osm.domain.root.indexed;

import junit.framework.TestCase;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import se.kodapan.osm.domain.OsmObject;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.PojoRoot;
import se.kodapan.osm.parser.xml.instantiated.InstantiatedOsmXmlParser;

import java.util.Map;

/**
 * @author kalle
 * @since 2013-10-20 02:23
 */
public class TestIndexedRoot extends TestCase {

  public void test() throws Exception {

    PojoRoot root = new PojoRoot();
    IndexedRoot<Query> index = IndexedRoot.newInstance(root);
    index.open();

    InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
    parser.setRoot(index);
    parser.parse(getClass().getResourceAsStream("/fjallbacka.osm.xml"));
    index.commit();

    assertEquals(36393, root.getNodes().size());
    assertEquals(4103, root.getWays().size());
    assertEquals(87, root.getRelations().size());


    Map<OsmObject, Float> hits = index.search(new MatchAllDocsQuery());
    assertEquals(root.getNodes().size() + root.getWays().size() + root.getRelations().size(), hits.size());


    Way building = index.getWay(198568340l);


    BooleanQuery bq;

    // not matching geographically


    bq = new BooleanQuery();
    bq.add(index.getQueryFactories().containsTagKeyQueryFactory().setKey("building").build(), BooleanClause.Occur.MUST);
    bq.add(index.getQueryFactories().wayEnvelopeQueryFactory()
        .setSouthLatitude(58.5952395).setWestLongitude(11.2786132)
        .setNorthLatitude(58.595755).setEastLongitude(11.2791668)
        .build(), BooleanClause.Occur.MUST);

    hits = index.search(bq);
    assertFalse(hits.keySet().contains(building));



    // encloses the building but search for highway

    bq = new BooleanQuery();
    bq.add(index.getQueryFactories().containsTagKeyQueryFactory().setKey("highway").build(), BooleanClause.Occur.MUST);
    bq.add(index.getQueryFactories().wayEnvelopeQueryFactory()
        .setSouthLatitude(58.5948092).setWestLongitude(11.279312)
        .setNorthLatitude(58.5951071).setEastLongitude(11.2802286)
        .build(), BooleanClause.Occur.MUST);

    hits = index.search(bq);
    assertFalse(hits.keySet().contains(building));



    // encloses the building

    bq = new BooleanQuery();
    bq.add(index.getQueryFactories().containsTagKeyQueryFactory().setKey("building").build(), BooleanClause.Occur.MUST);
    bq.add(index.getQueryFactories().wayEnvelopeQueryFactory()
        .setSouthLatitude(58.5948092).setWestLongitude(11.279312)
        .setNorthLatitude(58.5951071).setEastLongitude(11.2802286)
        .build(), BooleanClause.Occur.MUST);

    hits = index.search(bq);
    assertTrue(hits.keySet().contains(building));


    // encloses south west corner of building

    bq = new BooleanQuery();
    bq.add(index.getQueryFactories().containsTagKeyQueryFactory().setKey("building").build(), BooleanClause.Occur.MUST);
    bq.add(index.getQueryFactories().wayEnvelopeQueryFactory()
        .setSouthLatitude(58.5948159).setWestLongitude(11.2794345)
        .setNorthLatitude(58.5949187).setEastLongitude(11.2795752)
        .build(), BooleanClause.Occur.MUST);

    hits = index.search(bq);
    assertTrue(hits.keySet().contains(building));


    // encloses north east corner of building

    bq = new BooleanQuery();
    bq.add(index.getQueryFactories().containsTagKeyQueryFactory().setKey("building").build(), BooleanClause.Occur.MUST);
    bq.add(index.getQueryFactories().wayEnvelopeQueryFactory()
        .setSouthLatitude(58.5949749).setWestLongitude(11.279869)
        .setNorthLatitude(58.5950411).setEastLongitude(11.280004)
        .build(), BooleanClause.Occur.MUST);

    hits = index.search(bq);
    assertTrue(hits.keySet().contains(building));


    // encloses north west corner of building

    bq = new BooleanQuery();
    bq.add(index.getQueryFactories().containsTagKeyQueryFactory().setKey("building").build(), BooleanClause.Occur.MUST);
    bq.add(index.getQueryFactories().wayEnvelopeQueryFactory()
        .setSouthLatitude(58.5949784).setWestLongitude(11.2794515)
        .setNorthLatitude(58.5950735).setEastLongitude(11.2795888)
        .build(), BooleanClause.Occur.MUST);

    hits = index.search(bq);
    assertTrue(hits.keySet().contains(building));


    // encloses south east corner of building

    bq = new BooleanQuery();
    bq.add(index.getQueryFactories().containsTagKeyQueryFactory().setKey("building").build(), BooleanClause.Occur.MUST);
    bq.add(index.getQueryFactories().wayEnvelopeQueryFactory()
        .setSouthLatitude(58.5948425).setWestLongitude(11.2799007)
        .setNorthLatitude(58.5949016).setEastLongitude(11.2800471)
        .build(), BooleanClause.Occur.MUST);

    hits = index.search(bq);
    assertTrue(hits.keySet().contains(building));

    index.close();


  }

}
