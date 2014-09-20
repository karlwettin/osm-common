package se.kodapan.osm.util.json;

import junit.framework.TestCase;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.root.PojoRoot;
import se.kodapan.osm.domain.root.Root;
import se.kodapan.osm.parser.xml.instantiated.InstantiatedOsmXmlParser;

import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * @author kalle
 * @since 2013-11-02 14:18
 */
public class TestJsonMarshaller extends TestCase {

  public void test() throws Exception {

    JsonMarshaller marshaller = new JsonMarshaller();
    JsonUnmarshaller unmarshaller = new JsonUnmarshaller();

    PojoRoot root = new PojoRoot();
    InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
    parser.setRoot(root);

    parser.parse(new InputStreamReader(getClass().getResourceAsStream("/fjallbacka.osm.xml"), "UTF8"));

    Root.Enumerator<Node> nodes = root.enumerateNodes();
    Node node;
    while ((node = nodes.next()) != null) {

      StringWriter marshalled = new StringWriter();
      marshaller.serialize(node, marshalled);

//      Node unmarshalled = unmarshaller.deserializeNode(marshalled.toString());

    }

  }

}
