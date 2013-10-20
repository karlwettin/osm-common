package se.kodapan.osm.parser.xml.instantiated;


import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.osm.domain.*;
import se.kodapan.osm.parser.xml.OsmXmlParserException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
public abstract class AbstractStreamingInstantiatedOsmXmlParser extends InstantiatedOsmXmlParser {


  private static final Logger log = LoggerFactory.getLogger(AbstractStreamingInstantiatedOsmXmlParser.class);

  public Stream readerFactory(Reader xml) throws StreamException {
    return readerFactory(new ReaderInputStream(xml, "utf8"));
  }
  public Stream readerFactory(InputStream xml) throws StreamException {
    try {
      return readerFactory(new InputStreamReader(xml, "utf8"));
    } catch (UnsupportedEncodingException e) {
      throw new StreamException(e);
    }
  }

  public static class StreamException extends Exception {
    public StreamException() {
      super();    
    }

    public StreamException(String message) {
      super(message);    
    }

    public StreamException(String message, Throwable cause) {
      super(message, cause);    
    }
    public StreamException(Throwable cause) {
      super(cause);    
    }
  }
  public abstract static class Stream {
    public abstract int getEventType() throws StreamException;
    public abstract boolean isEndDocument(int eventType) throws StreamException;
    public abstract int next() throws StreamException;
    public abstract boolean isStartElement(int eventType) throws StreamException;
    public abstract boolean isEndElement(int eventType) throws StreamException;
    public abstract String getLocalName() throws StreamException;
    public abstract String getAttributeValue(String what, String key) throws StreamException;
    public abstract int getAttributeCount() throws StreamException;
    public abstract String getAttributeValue(int index) throws StreamException;
    public abstract String getAttributeLocalName(int index) throws StreamException;
    public abstract void close() throws StreamException;
  }
  

  public InstantiatedOsmXmlParserDelta parse(Reader xml) throws OsmXmlParserException {

    long started = System.currentTimeMillis();

    log.debug("Begin parsing...");

    InstantiatedOsmXmlParserDelta delta = new InstantiatedOsmXmlParserDelta();

    try {
      Stream xmlr = readerFactory(xml);

      OsmObject current = null;
      Node currentNode = null;
      Relation currentRelation = null;
      Way currentWay = null;

      boolean skipCurrentObject = false;

      State state = State.none;


      int eventType = xmlr.getEventType(); // START_DOCUMENT
      while (!xmlr.isEndDocument(eventType = xmlr.next())) {

        if (xmlr.isStartElement(eventType)) {

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

              currentNode = root.getNode(identity);
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
              }

              currentNode.setLatitude(Double.valueOf(xmlr.getAttributeValue(null, "lat")));
              currentNode.setLongitude(Double.valueOf(xmlr.getAttributeValue(null, "lon")));

              parseObjectAttributes(xmlr, currentNode, "id", "lat", "lon");

              currentNode.setLoaded(true);

              current = currentNode;

              delta.getCreatedNodes().add(currentNode);

              root.add(currentNode);

            } else if (state == State.modify) {

              currentNode = root.getNode(identity);

              if (currentNode == null) {
                throw new OsmXmlParserException("Inconsistency, node " + identity + " does not exists.");
              }

              Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
              if (version <= currentNode.getVersion()) {
                log.warn("Inconsistency, old version detected during modify node.");
                skipCurrentObject = true;
                continue;
              } else if (version > currentNode.getVersion() + 1 && !isAllowingMissingVersions()) {
                throw new OsmXmlParserException("Inconsistency, version " + version + " too great to modify node " + currentNode.getId() + " with version " + currentNode.getVersion());
              } else if (version.equals(currentNode.getVersion())) {
                throw new OsmXmlParserException("Inconsistency, found same version in new data during modify node.");

              }

              currentNode.setTags(null);
              currentNode.setAttributes(null);

              currentNode.setLatitude(Double.valueOf(xmlr.getAttributeValue(null, "lat")));
              currentNode.setLongitude(Double.valueOf(xmlr.getAttributeValue(null, "lon")));

              parseObjectAttributes(xmlr, currentNode, "id", "lat", "lon");

              current = currentNode;

              delta.getModifiedNodes().add(currentNode);

              root.add(currentNode);

            } else if (state == State.delete) {

              Node nodeToRemove = root.getNode(identity);

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
              } else if (version > nodeToRemove.getVersion() + 1 && !isAllowingMissingVersions()) {
                throw new OsmXmlParserException("Inconsistency, too great version found during delete node.");
              }


              root.remove(nodeToRemove);

              delta.getDeletedNodes().add(nodeToRemove);


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

              currentWay = root.getWay(identity);
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
                currentWay = new Way();
                currentWay.setId(identity);
              }

              parseObjectAttributes(xmlr, currentWay, "id");

              currentWay.setLoaded(true);

              current = currentWay;

              delta.getCreatedWays().add(currentWay);

            } else if (state == State.modify) {

              currentWay = root.getWay(identity);

              if (currentWay == null) {
                throw new OsmXmlParserException("Inconsistency, way " + identity + " does not exists.");
              }

              Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
              if (version <= currentWay.getVersion()) {
                log.warn("Inconsistency, old version detected during modify way.");
                skipCurrentObject = true;
                continue;
              } else if (version > currentWay.getVersion() + 1 && !isAllowingMissingVersions()) {
                throw new OsmXmlParserException("Inconsistency, found too great version in new data during modify way.");
              } else if (version.equals(currentWay.getVersion())) {
                throw new OsmXmlParserException("Inconsistency, found same version in new data during modify way.");
              }


              currentWay.setTags(null);
              currentWay.setAttributes(null);

              if (currentWay.getNodes() != null) {
                for (Node node : currentWay.getNodes()) {
                  node.getWaysMemberships().remove(currentWay);
                  root.add(node);
                }
              }
              currentWay.setNodes(null);

              parseObjectAttributes(xmlr, currentWay, "id");

              current = currentWay;

              delta.getModifiedWays().add(currentWay);

            } else if (state == State.delete) {

              Way wayToRemove = root.getWay(identity);

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
              } else if (version > wayToRemove.getVersion() + 1 && !isAllowingMissingVersions()) {
                throw new OsmXmlParserException("Inconsistency, too great way version found during delete way.");
              }


              root.remove(wayToRemove);

              delta.getDeletedWays().add(wayToRemove);

            }


          } else if ("nd".equals(xmlr.getLocalName())) {
            // a node reference inside of a way

            if (skipCurrentObject) {
              continue;
            }

            Long identity = Long.valueOf(xmlr.getAttributeValue(null, "ref"));

            if (state == State.none || state == State.create || state == State.modify) {

              Node node = root.getNode(identity);
              if (node == null) {
                node = new Node();
                node.setId(identity);
                root.add(node);
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

              currentRelation = root.getRelation(identity);
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
              }

              parseObjectAttributes(xmlr, currentRelation, "id");

              currentRelation.setLoaded(true);

              current = currentRelation;

              delta.getCreatedRelations().add(currentRelation);


            } else if (state == State.modify) {

              currentRelation = root.getRelation(identity);
              if (currentRelation == null) {
                throw new OsmXmlParserException("Inconsistency, relation " + identity + " does not exists.");
              }

              Integer version = Integer.valueOf(xmlr.getAttributeValue(null, "version"));
              if (version < currentRelation.getVersion()) {
                log.warn("Inconsistency, old version detected during modify relation.");
                skipCurrentObject = true;
                continue;
              } else if (version > currentRelation.getVersion() + 1 && !isAllowingMissingVersions()) {
                throw new OsmXmlParserException("Inconsistency, too great version found during modify relation.");
              } else if (version.equals(currentRelation.getVersion())) {
                throw new OsmXmlParserException("Inconsistency, same version found during modify relation.");
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

              delta.getModifiedRelations().add(currentRelation);

            } else if (state == State.delete) {

              Relation relationToRemove = root.getRelation(identity);

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
              } else if (version > relationToRemove.getVersion() + 1 && !isAllowingMissingVersions()) {
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

              root.remove(relationToRemove);
              delta.getDeletedRelations().add(relationToRemove);
            }


          } else if ("member".equals(xmlr.getLocalName())) {

            // multi polygon member

            if (skipCurrentObject) {
              continue;
            }

            if (state == State.none || state == State.create || state == State.modify) {

              RelationMembership member = new RelationMembership();
              member.setRelation(currentRelation);
              member.setRole(roleIntern.intern(xmlr.getAttributeValue(null, "role")));

              Long identity = Long.valueOf(xmlr.getAttributeValue(null, "ref"));
              String type = xmlr.getAttributeValue(null, "type");

              if ("way".equals(type)) {
                Way way = root.getWay(identity);
                if (way == null) {
                  way = new Way();
                  way.setId(identity);
                  root.add(way);
                }
                member.setObject(way);
              } else if ("node".equals(type)) {
                Node node = root.getNode(identity);
                if (node == null) {
                  node = new Node();
                  node.setId(identity);
                  root.add(node);
                }
                member.setObject(node);
              } else if ("relation".equals(type)) {
                Relation relation = root.getRelation(identity);
                if (relation == null) {
                  relation = new Relation();
                  relation.setId(identity);
                  root.add(relation);
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

              String key = tagKeyIntern.intern(xmlr.getAttributeValue(null, "k"));
              String value = tagValueIntern.intern(xmlr.getAttributeValue(null, "v"));
              current.setTag(key, value);

            } else if (state == State.delete) {
              //throw new OsmXmlParserException("Lexical error, delete object should not contain <tag> elements.");

            }
          }


        } else if (xmlr.isEndElement(eventType)) {

          if ("create".equals(xmlr.getLocalName())) {
            state = State.none;

          } else if ("modify".equals(xmlr.getLocalName())) {
            state = State.none;

          } else if ("delete".equals(xmlr.getLocalName())) {
            state = State.none;

          } else if ("node".equals(xmlr.getLocalName())) {

            if (state == State.none || state == State.create || state == State.modify) {
              root.add(currentNode);
            }

            processParsedNode(currentNode, state);
            currentNode = null;
            current = null;
            skipCurrentObject = false;


          } else if ("way".equals(xmlr.getLocalName())) {

            if (state == State.none || state == State.create || state == State.modify) {
              root.add(currentWay);
            }


            processParsedWay(currentWay, state);
            currentWay = null;
            current = null;
            skipCurrentObject = false;

          } else if ("relation".equals(xmlr.getLocalName())) {

            if (state == State.none || state == State.create || state == State.modify) {
              root.add(currentRelation);
            }


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
    } catch (StreamException ioe) {
      throw new OsmXmlParserException(ioe);
    }

    log.debug("Done parsing.");

    log.debug("Delta "
        + delta.getCreatedNodes().size() + "/"
        + delta.getModifiedNodes().size() + "/"
        + delta.getDeletedNodes().size() + " nodes, "

        + delta.getCreatedWays().size() + "/"
        + delta.getModifiedWays().size() + "/"
        + delta.getDeletedWays().size() + " ways, "

        + delta.getCreatedRelations().size() + "/"
        + delta.getModifiedRelations().size() + "/"
        + delta.getDeletedRelations().size() + " relations created/modified/deleted.");

    long timespent = System.currentTimeMillis() - started;

    log.info("Parsed in " + timespent + " milliseconds.");

    return delta;


  }


  private void parseObjectAttributes(Stream xmlr, OsmObject object, String... parsedAttributes) throws StreamException {

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
        object.setUser(userIntern.intern(value));

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

}

