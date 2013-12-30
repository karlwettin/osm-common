package se.kodapan.osm.util.json;

import org.apache.commons.lang3.StringEscapeUtils;
import se.kodapan.osm.domain.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * @author kalle
 * @since 2013-11-02 12:47
 */
public class JsonMarshaller {

  private OsmObjectVisitor<String> getTypeValueVisitor = new OsmObjectVisitor<String>() {
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

  public void serialize(Node node, Writer writer) throws IOException {

    writer.append("{");
    serializeOsmObject(node, writer);
    writer.append("\"latitude\":").append(String.valueOf(node.getLatitude())).append(",");
    writer.append("\"longitude\":").append(String.valueOf(node.getLongitude()));
    writer.append("}");

  }

  public void serialize(Way way, Writer writer) throws IOException {

    writer.append("{");
    serializeOsmObject(way, writer);
    if (way.getNodes() != null && !way.getNodes().isEmpty()) {
      writer.append("\"nodes\":[");
      for (Iterator<Node> iterator = way.getNodes().iterator(); iterator.hasNext(); ) {
        Node node = iterator.next();
        writer.append(String.valueOf(node.getId()));
        if (iterator.hasNext()) {
          writer.append(",");
        }
      }
    }
    writer.append("]");
    writer.append("}");

  }


  public void serialize(Relation relation, Writer writer) throws IOException {

    writer.append("{");
    serializeOsmObject(relation, writer);
    if (relation.getMembers() != null && !relation.getMembers().isEmpty()) {
      writer.append("\"members\"[");
      for (Iterator<RelationMembership> iterator = relation.getMembers().iterator(); iterator.hasNext(); ) {
        RelationMembership member = iterator.next();
        writer.append("{");
        writer.append("\"role\":\"").append(member.getRole()).append("\",");
        writer.append("\"type\":\"").append(member.getObject().accept(getTypeValueVisitor)).append("\",");
        writer.append("\"identity\":").append(String.valueOf(member.getObject().getId()));
        writer.append("}");
        if (iterator.hasNext()) {
          writer.append(",");
        }
      }
      writer.append("]");
    }
    serializeOsmObject(relation, writer);
    writer.append("}");

  }

  private void serializeOsmObject(OsmObject object, Writer writer) throws IOException {

    writer.append("\"type\":\"").append(object.accept(getTypeValueVisitor)).append("\",");
    writer.append("\"identity\":").append(String.valueOf(object.getId())).append(",");

    if (object.getTags() != null && !object.getTags().isEmpty()) {
      writer.append("\"tags\":[");
      for (Iterator<Map.Entry<String, String>> iterator = object.getTags().entrySet().iterator(); iterator.hasNext(); ) {
        Map.Entry<String, String> tag = iterator.next();
        writer.append("{");
        writer.append("\"key\":\"").append(StringEscapeUtils.escapeEcmaScript(tag.getKey())).append("\",");
        writer.append("\"value\":\"").append(StringEscapeUtils.escapeEcmaScript(tag.getValue())).append("\"");
        writer.append("}");
        if (iterator.hasNext()) {
          writer.append(",");
        }
      }
      writer.append("],");
    }

    if (object.getTimestamp() != null) {
      writer.append("\"timestamp\":").append(String.valueOf(object.getTimestamp())).append(",");
    }
    if (object.getChangeset() != null) {
      writer.append("\"changeset\":").append(String.valueOf(object.getChangeset())).append(",");
    }
    if (object.getUid() != null) {
      writer.append("\"uid\":").append(String.valueOf(object.getUid())).append(",");
    }
    if (object.getUser() != null) {
      writer.append("\"user\":\"").append(StringEscapeUtils.escapeEcmaScript(object.getUser())).append("\",");
    }
    if (object.getVersion() != null) {
      writer.append("\"version\":").append(String.valueOf(object.getVersion())).append(",");
    }
    if (object.getAttributes() != null && !object.getAttributes().isEmpty()) {
      writer.append("\"attributes\":[");
      for (Iterator<Map.Entry<String, String>> iterator = object.getAttributes().entrySet().iterator(); iterator.hasNext(); ) {
        Map.Entry<String, String> attribute = iterator.next();
        writer.append("{");
        writer.append("\"key\":\"").append(StringEscapeUtils.escapeEcmaScript(attribute.getKey())).append("\",");
        writer.append("\"value\":\"").append(StringEscapeUtils.escapeEcmaScript(attribute.getValue())).append("\"");
        writer.append("}");
        if (iterator.hasNext()) {
          writer.append(",");
        }

      }
      writer.append("],");
    }
    writer.append("\"visible\":").append(String.valueOf(object.isVisible())).append(",");
  }

}
