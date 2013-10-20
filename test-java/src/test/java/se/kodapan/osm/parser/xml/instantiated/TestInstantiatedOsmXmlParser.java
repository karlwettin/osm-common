package se.kodapan.osm.parser.xml.instantiated;

import junit.framework.TestCase;
import se.kodapan.osm.domain.root.PojoRoot;

import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * @author kalle
 * @since 2013-05-01 16:25
 */
public class TestInstantiatedOsmXmlParser extends TestCase {

  public void testBadData() throws Exception {

    InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();

    try {
      parser.parse(new StringReader("<foo>bar"));
      fail("Should throw an exception due to bad input data!");
    } catch (Exception e) {
      // all good
    }

  }

  public void testFjallbacka() throws Exception {

    PojoRoot root = new PojoRoot();
    InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
    parser.setRoot(root);

    parser.parse(new InputStreamReader(getClass().getResourceAsStream("/fjallbacka.osm.xml"), "UTF8"));

    assertEquals(36393, root.getNodes().size());
    assertEquals(4103, root.getWays().size());
    assertEquals(87, root.getRelations().size());

    System.currentTimeMillis();
  }


}
