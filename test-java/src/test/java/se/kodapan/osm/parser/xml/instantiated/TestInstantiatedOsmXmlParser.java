package se.kodapan.osm.parser.xml.instantiated;

import junit.framework.TestCase;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.RelationMembership;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.PojoRoot;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

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

  public void testMalmoe() throws Exception {

    PojoRoot root = new PojoRoot();
    InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
    parser.setRoot(root);

    parser.parse(new InputStreamReader(getClass().getResourceAsStream("/malmoe.osm.xml"), "UTF8"));

    Set<Way> ways = new HashSet<Way>();
    for (Node node : root.getNodes().values()) {
      ways.clear();
      if (node.getWaysMemberships() != null) {
        for (Way wayMember : node.getWaysMemberships()) {
          if (!ways.add(wayMember)) {
            fail("Multiple memberships of the same way!");
          }
        }
      }
    }

    Set<Relation> relations = new HashSet<Relation>();
    for (Node node : root.getNodes().values()) {
      relations.clear();
      if (node.getRelationMemberships() != null) {
        for (RelationMembership membership : node.getRelationMemberships()) {
          if (!relations.add(membership.getRelation())) {
            fail("Multiple memberships of the same relation!");
          }
        }
      }
    }
    for (Way way : root.getWays().values()) {
      relations.clear();
      if (way.getRelationMemberships() != null) {
        for (RelationMembership membership : way.getRelationMemberships()) {
          if (!relations.add(membership.getRelation())) {
            fail("Multiple memberships of the same relation!");
          }
        }
      }
    }
    for (Relation relation : root.getRelations().values()) {
      relations.clear();
      if (relation.getRelationMemberships() != null) {
        for (RelationMembership membership : relation.getRelationMemberships()) {
          if (!relations.add(membership.getRelation())) {
            fail("Multiple memberships of the same relation!");
          }
        }
      }
    }

    System.currentTimeMillis();
  }

}
