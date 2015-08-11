package se.kodapan.osm.jts;

import com.vividsolutions.jts.geom.MultiPolygon;
import junit.framework.TestCase;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.RelationMembership;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.PojoRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2015-06-03 05:19
 */
public class TestJtsGeometryFactory extends TestCase {

  public void test() throws Exception {


    Relation relation = new Relation();

    Way way = new Way();
    way.addNode(new Node(0,0));
    way.addNode(new Node(0,1));
    way.addNode(new Node(0,2));

    RelationMembership membership = new RelationMembership();
    membership.setRole("outer");
    membership.setObject(way);
    membership.setRelation(relation);
    relation.addMember(membership);
    way.addRelationMembership(membership);

    way = new Way();
    way.addNode(new Node(0,2));
    way.addNode(new Node(1,2));
    way.addNode(new Node(2,2));

    membership = new RelationMembership();
    membership.setRole("outer");
    membership.setObject(way);
    membership.setRelation(relation);
    relation.addMember(membership);
    way.addRelationMembership(membership);

    way = new Way();
    way.addNode(new Node(2,2));
    way.addNode(new Node(2,1));
    way.addNode(new Node(2,0));

    membership = new RelationMembership();
    membership.setRole("outer");
    membership.setObject(way);
    membership.setRelation(relation);
    relation.addMember(membership);
    way.addRelationMembership(membership);

    way = new Way();
    way.addNode(new Node(2,0));
    way.addNode(new Node(1,0));
    way.addNode(new Node(0,0));

    membership = new RelationMembership();
    membership.setRole("outer");
    membership.setObject(way);
    membership.setRelation(relation);
    relation.addMember(membership);
    way.addRelationMembership(membership);

    MultiPolygon multiPolygon = new JtsGeometryFactory().createMultiPolygon(relation);

    System.currentTimeMillis();

  }

}
