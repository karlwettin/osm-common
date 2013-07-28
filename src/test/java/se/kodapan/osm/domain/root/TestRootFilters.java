package se.kodapan.osm.domain.root;

import junit.framework.TestCase;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.services.nominatim.Nominatim;
import se.kodapan.osm.services.nominatim.NominatimJsonResponseParser;
import se.kodapan.osm.services.nominatim.NominatimQueryBuilder;

import java.util.List;

/**
 * @author kalle
 * @since 2013-07-27 21:49
 */
public class TestRootFilters extends TestCase {

  public void test() throws Exception {

    long id = 0;
    Root root = new Root();

    root.add(nodeFactory(id++, 10, 10, "a", "a"));
    root.add(nodeFactory(id++, 10, 10, "a", "b"));
    root.add(nodeFactory(id++, 10, 10, "b", "a"));
    root.add(nodeFactory(id++, 10, 10, "b", "b"));
    root.add(nodeFactory(id++, 10, 10, "b", "c"));

    assertEquals(2, root.filter(new ContainsTagKeyFilter("a")).size());
    assertEquals(3, root.filter(new ContainsTagKeyFilter("b")).size());
    assertEquals(0, root.filter(new ContainsTagKeyFilter("c")).size());

    assertEquals(2, root.filter(new ContainsTagValueFilter("a")).size());
    assertEquals(2, root.filter(new ContainsTagValueFilter("b")).size());
    assertEquals(1, root.filter(new ContainsTagValueFilter("c")).size());
    assertEquals(0, root.filter(new ContainsTagValueFilter("d")).size());

    assertEquals(1, root.filter(new ContainsTagKeyValueFilter("a", "a")).size());
    assertEquals(1, root.filter(new ContainsTagKeyValueFilter("a", "b")).size());
    assertEquals(0, root.filter(new ContainsTagKeyValueFilter("a", "c")).size());
    assertEquals(0, root.filter(new ContainsTagKeyValueFilter("d", "a")).size());

    root.add(nodeFactory(id++, 10, 10, "c", "q"));
    root.add(nodeFactory(id++, 10, 10, "c", "q"));
    root.add(nodeFactory(id++, 10, 10, "c", "q"));

    assertEquals(3, root.filter(new ContainsTagKeyValueFilter("c", "q")).size());
  }

  private Node nodeFactory(long id, double latitude, double longitude, String... tags) {
    Node node = new Node();
    node.setId(id);
    node.setLatitude(latitude);
    node.setLongitude(longitude);
    for (int i=0; i<tags.length; i+=2) {
      node.setTag(tags[i], tags[i+1]);
    }
    return node;
  }

}
