package se.kodapan.osm.parser.xml.instantiated;

import org.apache.commons.io.input.ReaderInputStream;
import se.kodapan.lang.Intern;
import se.kodapan.lang.InternImpl;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.PojoRoot;
import se.kodapan.osm.domain.root.Root;
import se.kodapan.osm.parser.xml.OsmXmlParserException;
import se.kodapan.osm.parser.xml.OsmXmlTimestampFormat;

import java.io.*;

/**
 * An .osm.xml and .osc.xml parser
 * into a fully instantiated object graph.
 *
 * @author kalle
 * @since 2013-03-27 21:41
 */
public abstract class InstantiatedOsmXmlParser {

  public static Class<InstantiatedOsmXmlParser> factoryClass;

  /**
   * @return a new instance depending on underlying OS. E.g. Android or Java.
   */
  public static InstantiatedOsmXmlParser newInstance() {
    synchronized (InstantiatedOsmXmlParser.class) {
      if (factoryClass == null) {
        try {
          factoryClass = (Class<InstantiatedOsmXmlParser>) Class.forName(InstantiatedOsmXmlParser.class.getName() + "Impl");
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    }
    try {
      return factoryClass.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * if false, OSM objects with a version greater than +1 of the current object in root will throw an exception.
   */
  protected boolean allowingMissingVersions = true;

  protected OsmXmlTimestampFormat timestampFormat = new OsmXmlTimestampFormat();

  protected Root root = new PojoRoot();

  protected Intern<String> tagKeyIntern = new InternImpl<String>();
  protected Intern<String> tagValueIntern = new InternImpl<String>();
  protected Intern<String> userIntern = new InternImpl<String>();
  protected Intern<String> roleIntern = new InternImpl<String>();

  /**
   * Overrides use to {@link #parse(java.io.Reader)}
   *
   * @param xml
   * @return
   * @throws OsmXmlParserException
   */
  public final InstantiatedOsmXmlParserDelta parse(String xml) throws OsmXmlParserException {
    return parse(new StringReader(xml));
  }

  /**
   * Overrides use to {@link #parse(java.io.Reader)}
   *
   * @param xml
   * @return
   * @throws OsmXmlParserException
   */
  public InstantiatedOsmXmlParserDelta parse(InputStream xml) throws OsmXmlParserException {
    try {
      return parse(new InputStreamReader(xml, "utf8"));
    } catch (UnsupportedEncodingException e) {
      throw new OsmXmlParserException(e);
    }
  }

  /**
   * Overrides use to {@link #parse(java.io.InputStream)}
   *
   * @param xml
   * @return
   * @throws OsmXmlParserException
   */
  public InstantiatedOsmXmlParserDelta parse(Reader xml) throws OsmXmlParserException {
    return parse(new ReaderInputStream(xml, "utf8"));
  }

  public enum State {
    none,
    create,
    modify,
    delete;
  }


  public void processParsedNode(Node node, State state) {
  }

  public void processParsedWay(Way way, State state) {
  }

  public void processParsedRelation(Relation relation, State state) {
  }


  public boolean isAllowingMissingVersions() {
    return allowingMissingVersions;
  }

  public void setAllowingMissingVersions(boolean allowingMissingVersions) {
    this.allowingMissingVersions = allowingMissingVersions;
  }

  public OsmXmlTimestampFormat getTimestampFormat() {
    return timestampFormat;
  }

  public void setTimestampFormat(OsmXmlTimestampFormat timestampFormat) {
    this.timestampFormat = timestampFormat;
  }

  public Root getRoot() {
    return root;
  }

  public void setRoot(Root root) {
    this.root = root;
  }

  public Intern<String> getTagKeyIntern() {
    return tagKeyIntern;
  }

  public void setTagKeyIntern(Intern<String> tagKeyIntern) {
    this.tagKeyIntern = tagKeyIntern;
  }

  public Intern<String> getTagValueIntern() {
    return tagValueIntern;
  }

  public void setTagValueIntern(Intern<String> tagValueIntern) {
    this.tagValueIntern = tagValueIntern;
  }

  public Intern<String> getUserIntern() {
    return userIntern;
  }

  public void setUserIntern(Intern<String> userIntern) {
    this.userIntern = userIntern;
  }

  public Intern<String> getRoleIntern() {
    return roleIntern;
  }

  public void setRoleIntern(Intern<String> roleIntern) {
    this.roleIntern = roleIntern;
  }
}


