package se.kodapan.osm.services.overpass;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Collection;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.OsmObject;
import se.kodapan.osm.domain.OsmObjectVisitor;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.Root;
import se.kodapan.osm.parser.xml.OsmXmlParserException;
import se.kodapan.osm.parser.xml.instantiated.InstantiatedOsmXmlParser;

/**
 * @author kalle
 * @since 2013-09-18 10:43 AM
 */
public class OverpassUtils {

  private Overpass overpass;

  public OverpassUtils(Overpass overpass) {
    this.overpass = overpass;
  }

  public void loadAllObjects(Root root) throws OverpassException, OsmXmlParserException {

    InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
    parser.setRoot(root);

    for (Node node : root.getNodes().values()) {
      if (!node.isLoaded()) {
        loadNode(parser, node.getId());
      }
    }
    for (Way way : root.getWays().values()) {
      if (!way.isLoaded()) {
        loadWay(parser, way.getId());
      }
    }
    for (Relation relation : root.getRelations().values()) {
      if (!relation.isLoaded()) {
        loadRelation(parser, relation.getId());
      }
    }
  }

  public Node getNode(long id) throws Exception {
    return loadNode(InstantiatedOsmXmlParser.newInstance(), id);
  }

  private static OsmObjectVisitor<String> getObjectType = new OsmObjectVisitor<String>() {
    @Override
    public String visit(Node node) {
      return "node";
    }

    @Override
    public String visit(Way way) {
      return "way";
    }

    @Override
    public String visit(Relation relation) {
      return "relation";
    }
  };

  public void loadObjects(InstantiatedOsmXmlParser parser, Collection<? extends OsmObject> osmObjects) throws OverpassException, OsmXmlParserException {

    StringWriter xml = new StringWriter(1000 + osmObjects.size() * 40);
    xml.write("<osm-script>\n");

    for (OsmObject osmObject : osmObjects) {
      xml.append("  <id-query ref=\"");
      xml.append("\" type=\"");
      xml.append(osmObject.accept(getObjectType));
      xml.append("\" />\n");
    }

    xml.append("  <print/>\n");
    xml.append("</osm-script>");

    String response = overpass.execute(xml.toString(), "Fetching " + osmObjects.size() + " objects by identity...");
    parser.parse(new StringReader(response));


  }

  public Node loadNode(InstantiatedOsmXmlParser parser, long id) throws OverpassException, OsmXmlParserException {

    String response = overpass.execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"node\"/>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting node " + id);

    parser.parse(new StringReader(response));
    return parser.getRoot().getNodes().get(id);

  }

  public Way loadWay(long id) throws OverpassException, OsmXmlParserException {
    return loadWay(InstantiatedOsmXmlParser.newInstance(), id);
  }

  public Way loadWay(InstantiatedOsmXmlParser parser, long id) throws OverpassException, OsmXmlParserException {

    parser.parse(new StringReader(overpass.execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"way\"/>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting way " + id)));

    parser.parse(new StringReader(overpass.execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"way\"/>\n" +
        "  <recurse type=\"way-node\"/>\n\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting way " + id)));


    return parser.getRoot().getWays().get(id);


  }

  public Relation loadRelation(long id) throws OverpassException, OsmXmlParserException {
    return loadRelation(InstantiatedOsmXmlParser.newInstance(), id);
  }


  public Relation loadRelation(InstantiatedOsmXmlParser parser, long id) throws OverpassException, OsmXmlParserException {


    parser.parse(new StringReader(overpass.execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"relation\"/>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting relation " + id)));

    parser.parse(new StringReader(overpass.execute("<osm-script>\n" +
        "  <id-query ref=\"" + id + "\" type=\"relation\"/>\n" +
        "  <recurse type=\"relation-way\"/>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting relation " + id + " ways")));

    parser.parse(new StringReader(overpass.execute("<osm-script>\n" +
        "  <union>\n" +
        "    <id-query ref=\"" + id + "\" type=\"relation\"/>\n" +
        "  <recurse type=\"relation-way\"/>\n" +
        "  <recurse type=\"way-node\"/>\n" +
        "  </union>\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting relation nodes" + id)));


    return parser.getRoot().getRelations().get(id);

  }


  public void loadEnvelope(InstantiatedOsmXmlParser parser, double latitudeSouth, double longitudeWest, double latitudeNorth, double longitudeEast) throws OverpassException, OsmXmlParserException {
    DecimalFormat df = new DecimalFormat("#.#####################");
    String bbox = "<bbox-query s=\"" + df.format(latitudeSouth) + "\" n=\"" + df.format(latitudeNorth) + "\" w=\"" + df.format(longitudeWest) + "\" e=\"" + df.format(longitudeEast) + "\"/>";

    parser.parse(new StringReader(overpass.execute("<osm-script>\n" +
        "  " + bbox + "\n" +
        "  <print/>\n" +
        "</osm-script>", "Getting nodes in bbox " + bbox)));

    parser.parse(new StringReader(overpass.execute("<osm-script>\n" +
        "  " + bbox + "\n" +
        "  <recurse type=\"node-way\"/>" +
        "  <print/>\n" +
        "</osm-script>", "Getting ways in bbox " + bbox)));

    parser.parse(new StringReader(overpass.execute("<osm-script>\n" +
        "  " + bbox + "\n" +
        "  <recurse type=\"node-relation\"/>" +
        "  <print/>\n" +
        "</osm-script>", "Getting relations in bbox " + bbox)));

  }
}
