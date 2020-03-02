package se.kodapan.osm.parser.xml.streaming;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.osm.domain.*;
import se.kodapan.osm.parser.xml.OsmXmlTimestampFormat;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.time.LocalDateTime;

/**
 * Parses .osm.xml files, but not .osc.xml files.
 * <p/>
 * This class is not thread safe!
 *
 * @author kalle
 * @since 2013-03-27 21:41
 */
public class StreamingOsmXmlParserImpl extends se.kodapan.osm.parser.xml.streaming.StreamingOsmXmlParser {

  private static final Logger log = LoggerFactory.getLogger(StreamingOsmXmlParserImpl.class);

  private OsmObject current;
  private Node currentNode;
  private Way currentWay;
  private Relation currentRelation;

  @Override
  public void read(Reader xml, StreamingOsmXmlParserListener listener) throws Exception {

    System.out.println("Reading xml.osm...");


    XMLInputFactory xmlif = XMLInputFactory.newInstance();

    XMLStreamReader xmlr = xmlif.createXMLStreamReader(xml);

    int eventType = xmlr.getEventType();
    while (xmlr.hasNext()) {
      eventType = xmlr.next();

      if (eventType == XMLStreamConstants.START_ELEMENT) {

        if ("node".equals(xmlr.getLocalName())) {

          currentNode = new Node();
          current = currentNode;
          currentNode.setId(Long.valueOf(xmlr.getAttributeValue(null, "id")));
          currentNode.setLatitude(Double.valueOf(xmlr.getAttributeValue(null, "lat")));
          currentNode.setLongitude(Double.valueOf(xmlr.getAttributeValue(null, "lon")));
          parseObjectAttributes(xmlr, currentNode, "id", "lat", "lon");


        } else if ("way".equals(xmlr.getLocalName())) {

          currentWay = new Way();
          current = currentWay;
          currentWay.setId(Long.valueOf(xmlr.getAttributeValue(null, "id")));
          parseObjectAttributes(xmlr, currentWay, "id");


        } else if ("nd".equals(xmlr.getLocalName())) {
          // a node reference inside of a way

          Node node = new Node();
          node.setId(Long.valueOf(xmlr.getAttributeValue(null, "ref")));
          node.addWayMembership(currentWay);
          currentWay.addNode(node);

        } else if ("relation".equals(xmlr.getLocalName())) {

          // multi polygon

          currentRelation = new Relation();
          current = currentRelation;
          currentRelation.setId(Long.valueOf(xmlr.getAttributeValue(null, "id")));
          parseObjectAttributes(xmlr, currentRelation, "id");


        } else if ("member".equals(xmlr.getLocalName())) {

          // multi polygon member

          RelationMembership member = new RelationMembership();
          member.setRelation(currentRelation);
          member.setRole(xmlr.getAttributeValue(null, "role").intern());

          String type = xmlr.getAttributeValue(null, "type");


          if ("way".equals(type)) {
            member.setObject(new Way());
          } else if ("node".equals(type)) {
            member.setObject(new Node());
          } else if ("relation".equals(type)) {
            member.setObject(new Relation());
          } else {
            throw new RuntimeException("Unsupported relation member type: " + type);
          }

          member.getObject().setId(Long.valueOf(xmlr.getAttributeValue(null, "ref")));
          member.getObject().addRelationMembership(member);
          currentRelation.addMember(member);


        } else if ("tag".equals(xmlr.getLocalName())) {

          // tag of any object type

          String key = xmlr.getAttributeValue(null, "k");
          String value = xmlr.getAttributeValue(null, "v");
          current.setTag(key, value);

        }


      } else if (eventType == XMLStreamConstants.END_ELEMENT) {

        if ("node".equals(xmlr.getLocalName())) {
          listener.processParsedNode(currentNode);
          currentNode = null;
          current = null;

        } else if ("way".equals(xmlr.getLocalName())) {
          listener.processParsedWay(currentWay);
          currentWay = null;
          current = null;

        } else if ("relation".equals(xmlr.getLocalName())) {
          listener.processParsedRelation(currentRelation);
          currentRelation = null;
          current = null;

        } else {
          // what not
        }

      }


    }

    xmlr.close();


  }

  private void parseObjectAttributes(XMLStreamReader xmlr, OsmObject object, String... parsedAttributes) throws ParseException {

    for (int attributeIndex = 0; attributeIndex < xmlr.getAttributeCount(); attributeIndex++) {
      String key = xmlr.getAttributeLocalName(attributeIndex);
      String value = xmlr.getAttributeValue(attributeIndex);

      if ("version".equals(key)) {
        object.setVersion(Integer.valueOf(value));

      } else if ("changeset".equals(key)) {
        object.setChangeset(Long.valueOf(value));

      } else if ("uid".equals(key)) {
        object.setUid(Long.valueOf(value));

      } else if ("user".equals(key)) {
        object.setUser(value);

      } else if ("visible".equals(key)) {
        object.setVisible(Boolean.valueOf(value));

      } else if ("timestamp".equals(key)) {
        if (value.endsWith("Z")) {
          value = value.substring(0, value.length() -1 );
        }
        object.setTimestamp(LocalDateTime.parse(value));

      } else {

        boolean parsed = false;
        for (String parsedAttribute : parsedAttributes) {
          if (parsedAttribute.equals(key)) {
            parsed = true;
            break;
          }
        }
        if (!parsed) {
          object.setAttribute(key, value);
          log.warn("Unknown attribute " + key + "='" + value + "' added to object");
        }

      }
    }

  }


}