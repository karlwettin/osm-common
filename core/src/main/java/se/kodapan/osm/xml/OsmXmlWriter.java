package se.kodapan.osm.xml;

import org.apache.commons.lang3.StringEscapeUtils;
import se.kodapan.osm.domain.*;
import se.kodapan.osm.domain.root.PojoRoot;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author kalle
 * @since 2013-09-02 5:03 PM
 */
public class OsmXmlWriter extends Writer {

  private Writer xml;
  private String version = "0.6";
  private Boolean upload = true;
  private String generator = getClass().getName();

  public OsmXmlWriter(Writer xml) throws IOException {
    this.xml = xml;
    writeHeader();
  }

  public void writeHeader() throws IOException {
    xml.write("<?xml version='1.0' encoding='UTF-8'?>\n");
    xml.write("<osm version='");
    xml.write(version);
    if (upload != null) {
      xml.write("' upload='");
      xml.write(upload ? "true" : "false");
      xml.write("'");
    }
    xml.write(" generator='");
    xml.write(generator);
    xml.write("'>\n");
  }


  public void writeFooter() throws IOException {
    xml.write("</osm>\n");
  }


  private OsmObjectVisitor<Void> writeVisitor = new OsmObjectVisitor<Void>() {
    @Override
    public Void visit(Node node) {
      try {
        write(node);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
      return null;
    }

    @Override
    public Void visit(Way way) {
      try {
        write(way);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
      return null;
    }

    @Override
    public Void visit(Relation relation) {
      try {
        write(relation);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
      return null;
    }
  };

  public void write(OsmObject object) throws IOException {
    object.accept(writeVisitor);
  }

  public void write(PojoRoot root) throws IOException {
    for (Node node : root.getNodes().values()) {
      write(node);
    }
    for (Way way : root.getWays().values()) {
      write(way);
    }
    for (Relation relation : root.getRelations().values()) {
      write(relation);
    }
  }

  public void writeTags(OsmObject osmObject) throws IOException {
    // <tag k='landuse' v='farmland' />

    if (osmObject.getTags() != null) {
      for (Map.Entry<String, String> tag : osmObject.getTags().entrySet()) {
        xml.write("\t\t<tag k='");
        xml.write(tag.getKey());
        xml.write("' v='");
        xml.write(StringEscapeUtils.escapeXml(tag.getValue()));
        xml.write("' />\n");
      }
    }
  }

  public void write(Node node) throws IOException {
    writeObjectHead(node);

    xml.write(" lat='");
    xml.write(new DecimalFormat("#.##################################").format(node.getLatitude()));
    xml.write("'");

    xml.write(" lon='");
    xml.write(new DecimalFormat("#.##################################").format(node.getLongitude()));
    xml.write("'");

    xml.write(" >\n");


    writeTags(node);
    xml.write("\t</node>\n");

  }

  public void write(Way way) throws IOException {

    writeObjectHead(way);
    xml.write(" >\n");

    for (Node node : way.getNodes()) {
      xml.append("\t\t<nd ref='");
      xml.append(String.valueOf(node.getId()));
      xml.append("' />\n");
      node.getId();
    }

    writeTags(way);

    xml.write("\t</way>\n");

  }

  private OsmObjectVisitor<String> getOsmObjectTypeName = new OsmObjectVisitor<String>() {
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

  public void write(Relation relation) throws IOException {

// <relation id='3146471' timestamp='2013-08-16T01:39:33Z' uid='194367' user='Karl Wettin' visible='true' version='1' changeset='17366616'>

    writeObjectHead(relation);
    xml.write(" >\n");


    for (RelationMembership membership : relation.getMembers()) {

      xml.write("\t\t<member type='");
      xml.write(membership.getObject().accept(getOsmObjectTypeName));
      xml.write("'");

      xml.write(" ref='");
      xml.write(String.valueOf(membership.getObject().getId()));
      xml.write("'");

      xml.write(" role='");
      xml.write(membership.getRole());
      xml.write("'");


      xml.write(" />\n");
    }

    writeTags(relation);

    xml.write("\t</relation>\n");
  }

  private void writeObjectHead(OsmObject osmObject) throws IOException {
    xml.write("\t<");
    xml.append(osmObject.accept(getOsmObjectTypeName));

    if (osmObject.getId() != null) {
      xml.write(" id='");
      xml.write(String.valueOf(osmObject.getId()));
      xml.write("'");
    }

    if (osmObject.getTimestamp() != null) {
      xml.write(" timestamp='");
      xml.write(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(osmObject.getTimestamp())));
      xml.write("'");
    }

    if (osmObject.getUid() != null) {
      xml.write(" uid='");
      xml.write(String.valueOf(osmObject.getUid()));
      xml.write("'");
    }

    if (osmObject.getUser() != null) {
      xml.write(" user='");
      xml.write(String.valueOf(osmObject.getUser()));
      xml.write("'");
    }

    if (osmObject.getVersion() != null) {
      xml.write(" version='");
      xml.write(String.valueOf(osmObject.getVersion()));
      xml.write("'");
    }

    if (osmObject.getChangeset() != null) {
      xml.write(" changeset='");
      xml.write(String.valueOf(osmObject.getChangeset()));
      xml.write("'");
    }

  }


  private boolean wroteHeader = false;

  @Override
  public synchronized void write(char[] cbuf, int off, int len) throws IOException {
    if (!wroteHeader) {
      wroteHeader = true;
      writeHeader();
    }
    xml.write(cbuf, off, len);
  }

  @Override
  public void flush() throws IOException {
    xml.flush();
  }

  @Override
  public void close() throws IOException {
    writeFooter();
    xml.close();
  }

}