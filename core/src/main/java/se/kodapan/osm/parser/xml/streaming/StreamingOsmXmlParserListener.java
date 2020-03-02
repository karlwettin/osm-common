package se.kodapan.osm.parser.xml.streaming;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;

import java.util.Map;

/**
 * @author kalle
 * @since 2013-10-16 20:44
 */
public class StreamingOsmXmlParserListener {

  public void processParsedNode(Node node) {
  }

  public void processParsedWay(Way way) {
  }

  public void processParsedRelation(Relation relation) {
  }

  public void processParsedChangeset(Map<String, String> changeset) {
  }

}
