package se.kodapan.osm.services.api.v_0_6;

import junit.framework.TestCase;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.root.Root;
import se.kodapan.osm.util.Coordinate;

import java.util.UUID;

/**
 * @author kalle
 * @since 2015-01-06 22:55
 */
public class TestApiConnection extends TestCase {

  private double malmoSouth = 55.600171823967;
  private double malmoWest = 12.992362976074219;
  private double malmoNorth = 55.60928712815964;
  private double malmoEast = 13.012619018554688;

  public void test() throws Exception {

//    ApiConnection api = new ApiConnection("http://api06.dev.openstreetmap.org/api");
//    ApiConnection api = new ApiConnection();
    ApiConnection api = new ApiConnection("http://api06.dev.openstreetmap.org/api");

    Root root = api.get(malmoSouth, malmoWest, malmoNorth, malmoEast);

    api.authenticate("osm-common@kodapan.se", "osm-common");


    long changeset = api.createChangeset("Test");
    try {

      String reference = UUID.randomUUID().toString();

      Node node = new Node();
      node.setChangeset(changeset);
      node.setLongitude(53);
      node.setLatitude(16);
      node.setTag("ref:osm-common", reference);

      api.create(changeset, node);

      assertTrue(node.getId() > 0);
      assertEquals(new Long(changeset), node.getChangeset());
      assertEquals(reference, node.getTag("ref:osm-common"));

      Node newNode = api.getNode(node.getId());
      assertEquals(node.getId(), newNode.getId());
      assertEquals(new Long(changeset), newNode.getChangeset());
      assertEquals(node.getLatitude(), newNode.getLatitude());
      assertEquals(node.getLongitude(), newNode.getLongitude());
      assertEquals(reference, newNode.getTag("ref:osm-common"));

      api.delete(changeset, newNode);

      // todo check deleted

      System.currentTimeMillis();

    } finally {
      api.closeChangeset(changeset);
    }

    System.currentTimeMillis();
  }

}
