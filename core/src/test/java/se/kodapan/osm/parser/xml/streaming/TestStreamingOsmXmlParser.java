package se.kodapan.osm.parser.xml.streaming;

import junit.framework.TestCase;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;

import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kalle
 * @since 2013-05-01 16:25
 */
public class TestStreamingOsmXmlParser extends TestCase {

  public void test() throws Exception {

    final AtomicInteger nodesCounter = new AtomicInteger();
    final AtomicInteger waysCounter = new AtomicInteger();
    final AtomicInteger relationsCounter = new AtomicInteger();

    StreamingOsmXmlParser parser = new StreamingOsmXmlParser() {
      @Override
      public void processParsedNode(Node node) {
        nodesCounter.incrementAndGet();
      }

      @Override
      public void processParsedWay(Way way) {
        waysCounter.incrementAndGet();
      }

      @Override
      public void processParsedRelation(Relation relation) {
        relationsCounter.incrementAndGet();
      }
    };
    parser.read(new InputStreamReader(getClass().getResourceAsStream("/fjallbacka.osm.xml"), "UTF8"));

    assertEquals(36393, nodesCounter.get());
    assertEquals(2012, waysCounter.get());
    assertEquals(75, relationsCounter.get());

  }

}
