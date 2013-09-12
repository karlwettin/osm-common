package se.kodapan.osm.data.planet.parser.xml.instantiated;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.osm.domain.*;
import se.kodapan.osm.data.planet.parser.xml.OsmXmlParserException;
import se.kodapan.osm.data.planet.parser.xml.OsmXmlTimestampFormat;
import se.kodapan.osm.domain.root.Root;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;

/**
 * An .osm.xml and .osc.xml parser
 * into a fully instantiated object graph.
 * <p/>
 * This class is not thread safe!
 *
 * @author kalle
 * @since 2013-03-27 21:41
 */
public class InstantiatedOsmXmlParser {

  private static final Logger log = LoggerFactory.getLogger(InstantiatedOsmXmlParser.class);

  private OsmXmlTimestampFormat timestampFormat = new OsmXmlTimestampFormat();

  private Root root = new Root();

  private enum State {
    none,
    create,
    modify,
    delete;
  }

  public InstantiatedOsmXmlParserDelta parse(String xml) throws OsmXmlParserException {
    return parse(new StringReader(xml));
  }
  public InstantiatedOsmXmlParserDelta parse(Reader xml) throws OsmXmlParserException {

    long started = System.currentTimeMillis();

    log.debug("Begin parsing...");

    InstantiatedOsmXmlParserDelta delta = new InstantiatedOsmXmlParserDelta();

    XMLInputFactory xmlif = XMLInputFactory.newInstance();

    try {
    XMLStreamReader xmlr = xmlif.createXMLStreamReader(xml);

    OsmObject current = null;
    Node currentNode = null;
    Relation currentRelation = null;
    Way currentWay = null;

    boolean skipCurrentObject = false;

    State state = State.none;


    int eventType = xmlr.getEventType();
    while (xmlr.hasNext()) {
      eventType = xmlr.next();

      if (eventType == XMLStreamConstants.START_ELEMENT) {

        if ("create".equals(xmlr.getLocalName())) {
          state = State.create;

        } else if ("modify".equals(xmlr.getLocalName())) {
          state = State.modify;

        } else if ("delete".equals(xmlr.getLocalName())) {
          state = State.delete;

        } else if ("node".equals(xmlr.getLocalName())) {

          /**
           *
           *   NN  NN   OOOO   DDDDD  EEEEE
           *   NNN NN  OO  OO  DD  DD EE
           *   NNNNNN  OO  OO  DD  DD EEEE
           *   NN NNN  OO  OO  DD  DD EE
           *   NN  NN   OOOO   DDDDD  EEEEE
           */


          Long identity = Long.valueOf(xmlr.getAttributeValue(null, "id"));

          if (state == State.none || state == State.create) {

            currentNode = root.getNodes().get(identity);
            if (currentNode != null && currentNode.isLoaded() && currentNode.getVersion() != null) {

              Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
              if (version <= currentNode.getVersion()) {
                log.warn("Inconsistency, old version detected during create node.");
                skipCurrentObject = true;
                continue;
//              } else if (version > currentNode.getVersion() + 1) {
//                throw new OsmXmlParserException("Inconsistency, too great version found during create node.");
              } else {
                throw new OsmXmlParserException("Inconsistency, node " + identity + " already exists.");
              }

            }
            if (currentNode == null) {
              currentNode = new Node();
              currentNode.setId(identity);
              root.getNodes().put(identity, currentNode);
            }

            currentNode.setLatitude(Double.valueOf(xmlr.getAttributeValue(null, "lat")));
            currentNode.setLongitude(Double.valueOf(xmlr.getAttributeValue(null, "lon")));

            parseObjectAttributes(xmlr, currentNode, "id", "lat", "lon");

            currentNode.setLoaded(true);

            current = currentNode;

            delta.getCreatedNodeIdentities().add(identity);

          } else if (state == State.modify) {

            currentNode = root.getNodes().get(identity);

            if (currentNode == null) {
              throw new OsmXmlParserException("Inconsistency, node " + identity + " does not exists.");
            }

            Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
            if (version <= currentNode.getVersion()) {
              log.warn("Inconsistency, old version detected during modify node.");
              skipCurrentObject = true;
              continue;
            } else if (version > currentNode.getVersion() + 1) {
              throw new OsmXmlParserException("Inconsistency, version " + version + " too great to modify node " + currentNode.getId() + " with version " + currentNode.getVersion());
            }

            currentNode.setTags(null);
            currentNode.setAttributes(null);

            currentNode.setLatitude(Double.valueOf(xmlr.getAttributeValue(null, "lat")));
            currentNode.setLongitude(Double.valueOf(xmlr.getAttributeValue(null, "lon")));

            parseObjectAttributes(xmlr, currentNode, "id", "lat", "lon");

            current = currentNode;

            delta.getModifiedNodeIdentities().add(identity);

          } else if (state == State.delete) {

            Node nodeToRemove = root.getNodes().get(identity);

            if (nodeToRemove == null) {
              log.warn("Inconsistency, node " + identity + " does not exists.");
              skipCurrentObject = true;
              continue;
            }

            Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
            if (version < nodeToRemove.getVersion()) {
              log.warn("Inconsistency, old version detected during delete node.");
              skipCurrentObject = true;
              continue;
            } else if (version > nodeToRemove.getVersion() + 1) {
              throw new OsmXmlParserException("Inconsistency, too great version found during delete node.");
            }


            if (nodeToRemove.getWaysMemberships() != null) {
              for (Way way : nodeToRemove.getWaysMemberships()) {
                way.getNodes().remove(nodeToRemove);
              }
            }
            nodeToRemove.setWaysMemberships(null);

            if (nodeToRemove.getRelationMemberships() != null) {
              for (RelationMembership member : nodeToRemove.getRelationMemberships()) {
                member.getRelation().getMembers().remove(member);
              }
            }
            nodeToRemove.setRelationMemberships(null);

            Node removedNode = root.getNodes().remove(identity);

            delta.getDeletedNodeIdentities().add(identity);
          }

        } else if ("way".equals(xmlr.getLocalName())) {

          /**
           *
           *   WW  WW  WW   AA   YY  YY
           *    WW WW WW   AAAA   YYYY
           *    WWWWWWWW  AA  AA   YY
           *     WW  WW   AAAAAA   YY
           *     WW  WW   AA  AA   YY
           */


          long identity = Long.valueOf(xmlr.getAttributeValue(null, "id"));

          if (state == State.none || state == State.create) {

            currentWay = root.getWays().get(identity);
            if (currentWay != null && currentWay.isLoaded() && currentWay.getVersion() != null) {

              Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
              if (version <= currentWay.getVersion()) {
                log.warn("Inconsistency, old version detected during create way.");
                skipCurrentObject = true;
                continue;
//              } else if (version > currentWay.getVersion() + 1) {
//                throw new OsmXmlParserException("Inconsistency, too great version found during create way.");
              } else {
                throw new OsmXmlParserException("Inconsistency, way " + identity + " already exists.");
              }


            }
            if (currentWay == null) {
              currentWay = createWay(identity);
            }

            parseObjectAttributes(xmlr, currentWay, "id");

            currentWay.setLoaded(true);

            current = currentWay;

            delta.getCreatedWayIdentities().add(identity);

          } else if (state == State.modify) {

            currentWay = root.getWays().get(identity);

            if (currentWay == null) {
              throw new OsmXmlParserException("Inconsistency, way " + identity + " does not exists.");
            }

            Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
            if (version <= currentWay.getVersion()) {
              log.warn("Inconsistency, old version detected during modify way.");
              skipCurrentObject = true;
              continue;
            } else if (version > currentWay.getVersion() + 1) {
              throw new OsmXmlParserException("Inconsistency, too great version found during modify way.");
            }


            currentWay.setTags(null);
            currentWay.setAttributes(null);

            if (currentWay.getNodes() != null) {
              for (Node node : currentWay.getNodes()) {
                node.getWaysMemberships().remove(currentWay);
              }
            }
            currentWay.setNodes(null);

            parseObjectAttributes(xmlr, currentWay, "id");

            current = currentWay;

            delta.getModifiedWayIdentities().add(identity);


          } else if (state == State.delete) {

            Way wayToRemove = root.getWays().get(identity);

            if (wayToRemove == null) {
              log.warn("Inconsistency, way " + identity + " does not exists.");
              skipCurrentObject = true;
              continue;
            }

            Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
            if (version < wayToRemove.getVersion()) {
              log.warn("Inconsistency, old version detected during delete way.");
              skipCurrentObject = true;
              continue;
            } else if (version > wayToRemove.getVersion() + 1) {
              throw new OsmXmlParserException("Inconsistency, too great way version found during delete way.");
            }


            if (wayToRemove.getNodes() != null) {
              for (Node node : wayToRemove.getNodes()) {
                node.getWaysMemberships().remove(wayToRemove);
              }
              wayToRemove.setNodes(null);
            }

            if (wayToRemove.getRelationMemberships() != null) {
              for (RelationMembership member : wayToRemove.getRelationMemberships()) {
                member.getRelation().getMembers().remove(member);
              }
              wayToRemove.setRelationMemberships(null);
            }

            Way removedWay = root.getWays().remove(identity);

            delta.getDeletedWayIdentities().add(identity);

          }


        } else if ("nd".equals(xmlr.getLocalName())) {
          // a node reference inside of a way

          if (skipCurrentObject) {
            continue;
          }

          Long identity = Long.valueOf(xmlr.getAttributeValue(null, "ref"));

          if (state == State.none || state == State.create || state == State.modify) {

            Node node = root.getNodes().get(identity);
            if (node == null) {
              node = new Node();
              node.setId(identity);
              root.getNodes().put(identity, node);
            }
            node.addWayMembership(currentWay);
            currentWay.addNode(node);

          } else if (state == State.delete) {
            //throw new OsmXmlParserException("Lexical error, delete way should not contain <nd> elements.");

          }


        } else if ("relation".equals(xmlr.getLocalName())) {

          /**
           *
           *   RRRRR  EEEEEE  LL       AA   TTTTTTTT  II   OOOOO   NN  NN
           *   RR  RR EE      LL      AAAA     TT     II  OO   OO  NNN NN
           *   RRRRR  EEEEEE  LL     AA  AA    TT     II  OO   OO  NNNNNN
           *   RR  RR EE      LL     AAAAAA    TT     II  OO   OO  NN NNN
           *   RR  RR EEEEEE  LLLLL  AA  AA    TT     II   OOOOO   NN  NN
           */


          // multi polygon, etc

          Long identity = Long.valueOf(xmlr.getAttributeValue(null, "id"));

          if (state == State.none || state == State.create) {

            currentRelation = root.getRelations().get(identity);
            if (currentRelation != null && currentRelation.isLoaded() && currentRelation.getVersion() != null) {

              Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
              if (version <= currentRelation.getVersion()) {
                log.warn("Inconsistency, old version detected during create relation.");
                skipCurrentObject = true;
                continue;
//              } else if (version > currentRelation.getVersion() + 1) {
//                throw new OsmXmlParserException("Inconsistency, too great version found during create relation.");
              } else {
                throw new OsmXmlParserException("Inconsistency, relation " + identity + " already exists.");
              }

            }

            if (currentRelation == null) {
              currentRelation = new Relation();
              currentRelation.setId(identity);
              root.getRelations().put(identity, currentRelation);
            }

            parseObjectAttributes(xmlr, currentRelation, "id");

            currentRelation.setLoaded(true);

            current = currentRelation;

            delta.getCreatedRelationIdentities().add(identity);

          } else if (state == State.modify) {

            currentRelation = root.getRelations().get(identity);
            if (currentRelation == null) {
              throw new OsmXmlParserException("Inconsistency, relation " + identity + " does not exists.");
            }

            Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
            if (version < currentRelation.getVersion()) {
              log.warn("Inconsistency, old version detected during modify relation.");
              skipCurrentObject = true;
              continue;
            } else if (version > currentRelation.getVersion() + 1) {
              throw new OsmXmlParserException("Inconsistency, too great version found during modify relation.");
            }

            if (currentRelation.getMembers() != null) {
              for (RelationMembership member : currentRelation.getMembers()) {
                member.getObject().getRelationMemberships().remove(member);
                if (member.getObject().getRelationMemberships().isEmpty()) {
                  member.getObject().setRelationMemberships(null);
                }
              }
              currentRelation.setMembers(null);
            }
            currentRelation.setAttributes(null);
            currentRelation.setTags(null);

            current = currentRelation;


            parseObjectAttributes(xmlr, currentRelation, "id");

            delta.getModifiedRelationIdentities().add(identity);

          } else if (state == State.delete) {

            Relation relationToRemove = root.getRelations().get(identity);

            if (relationToRemove == null) {
              log.warn("Inconsistency, relation " + identity + " does not exist.");
              skipCurrentObject = true;
              continue;
            }

            Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
            if (version < relationToRemove.getVersion()) {
              log.warn("Inconsistency, old version detected during delete relation.");
              skipCurrentObject = true;
              continue;
            } else if (version > relationToRemove.getVersion() + 1) {
              throw new OsmXmlParserException("Inconsistency, too great version found during delete relation.");
            }

            if (relationToRemove.getMembers() != null) {
              for (RelationMembership member : relationToRemove.getMembers()) {
                member.getObject().getRelationMemberships().remove(member);
                if (member.getObject().getRelationMemberships().isEmpty()) {
                  member.getObject().setRelationMemberships(null);
                }
              }
              relationToRemove.setMembers(null);
            }

            Relation removedRelation = root.getRelations().remove(identity);

            delta.getDeletedRelationIdentities().add(identity);
          }


        } else if ("member".equals(xmlr.getLocalName())) {

          // multi polygon member

          if (skipCurrentObject) {
            continue;
          }

          if (state == State.none || state == State.create || state == State.modify) {

            RelationMembership member = new RelationMembership();
            member.setRelation(currentRelation);
            member.setRole(xmlr.getAttributeValue(null, "role").intern());

            Long identity = Long.valueOf(xmlr.getAttributeValue(null, "ref"));
            String type = xmlr.getAttributeValue(null, "type");

            if ("way".equals(type)) {
              Way way = root.getWays().get(identity);
              if (way == null) {
                way = createWay(identity);
              }
              member.setObject(way);
            } else if ("node".equals(type)) {
              Node node = root.getNodes().get(identity);
              if (node == null) {
                node = new Node();
                node.setId(identity);
                root.getNodes().put(identity, node);
              }
              member.setObject(node);
            } else if ("relation".equals(type)) {
              Relation relation = root.getRelations().get(identity);
              if (relation == null) {
                relation = new Relation();
                relation.setId(identity);
                root.getRelations().put(identity, relation);
              }
              member.setObject(relation);

            } else {
              throw new RuntimeException("Unsupported relation member type: " + type);
            }

            member.getObject().addRelationMembership(member);
            currentRelation.addMember(member);


          } else if (state == State.delete) {
            //throw new OsmXmlParserException("Lexical error, delete relation should not contain <member> elements.");

          }


        } else if ("tag".equals(xmlr.getLocalName())) {

          // tag of any object type

          if (skipCurrentObject) {
            continue;
          }

          if (state == State.none || state == State.create || state == State.modify) {

            String key = root.getTagKeyIntern().intern(xmlr.getAttributeValue(null, "k"));
            String value = root.getTagValueIntern().intern(xmlr.getAttributeValue(null, "v"));
            current.setTag(key, value);

          } else if (state == State.delete) {
            //throw new OsmXmlParserException("Lexical error, delete object should not contain <tag> elements.");

          }
        }


      } else if (eventType == XMLStreamConstants.END_ELEMENT) {

        if ("create".equals(xmlr.getLocalName())) {
          state = State.none;

        } else if ("modify".equals(xmlr.getLocalName())) {
          state = State.none;

        } else if ("delete".equals(xmlr.getLocalName())) {
          state = State.none;

        } else if ("node".equals(xmlr.getLocalName())) {

          processParsedNode(currentNode, state);
          currentNode = null;
          current = null;
          skipCurrentObject = false;


        } else if ("way".equals(xmlr.getLocalName())) {

          processParsedWay(currentWay, state);
          currentWay = null;
          current = null;
          skipCurrentObject = false;

        } else if ("relation".equals(xmlr.getLocalName())) {

          processParsedRelation(currentRelation, state);
          currentRelation = null;
          current = null;
          skipCurrentObject = false;

        } else {
          // what not
        }

      }


    }

    xmlr.close();
  } catch (XMLStreamException ioe) {
    throw new OsmXmlParserException(ioe);
  }

    log.debug("Done parsing.");

    log.debug("Delta "
        + delta.getCreatedNodeIdentities().size() + "/"
        + delta.getModifiedNodeIdentities().size() + "/"
        + delta.getDeletedNodeIdentities().size() + " nodes, "

        + delta.getCreatedWayIdentities().size() + "/"
        + delta.getModifiedWayIdentities().size() + "/"
        + delta.getDeletedWayIdentities().size() + " ways, "

        + delta.getCreatedRelationIdentities().size() + "/"
        + delta.getModifiedRelationIdentities().size() + "/"
        + delta.getDeletedRelationIdentities().size() + " relations created/modified/deleted.");

    long timespent = System.currentTimeMillis() - started;

    log.info("Parsed in " + timespent + " milliseconds. Root now contains " + root.getNodes().size() + " nodes, " + root.getWays().size() + " ways and " + root.getRelations().size() + " relations.");


    return delta;


  }

  private Way createWay(long identity) {
    Way currentWay = new Way();
    currentWay.setId(identity);
    root.getWays().put(identity, currentWay);
    return currentWay;
  }

  private void parseObjectAttributes(XMLStreamReader xmlr, OsmObject object, String... parsedAttributes)  {

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
        try {
          object.setTimestamp(timestampFormat.parse(value).getTime());
        } catch (ParseException pe) {
          throw new RuntimeException(pe);
        }

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

  public void processParsedNode(Node node, State state) {
  }

  public void processParsedWay(Way way, State state) {
  }

  public void processParsedRelation(Relation relation, State state) {
  }


  public Root getRoot() {
    return root;
  }

  public void setRoot(Root root) {
    this.root = root;
  }

}

