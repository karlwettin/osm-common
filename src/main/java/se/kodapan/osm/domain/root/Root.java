package se.kodapan.osm.domain.root;

import se.kodapan.lang.Intern;
import se.kodapan.lang.InternImpl;
import se.kodapan.osm.domain.*;

import java.io.Serializable;
import java.util.*;

/**
 * @author kalle
 * @since 2013-05-01 21:29
 */
public class Root implements Serializable {

  private static final long serialVersionUID = 1l;

  private Intern<String> tagKeyIntern = new InternImpl<String>();
  private Intern<String> tagValueIntern = new InternImpl<String>();

  private Map<Long, Node> nodes = new HashMap<Long, Node>();
  private Map<Long, Way> ways = new HashMap<Long, Way>();
  private Map<Long, Relation> relations = new HashMap<Long, Relation>();

  private OsmObjectVisitor<Void> addVisitor = new OsmObjectVisitor<Void>() {
    @Override
    public Void visit(Node node) {
      getNodes().put(node.getId(), node);
      return null;
    }

    @Override
    public Void visit(Way way) {
      getWays().put(way.getId(), way);
      return null;
    }

    @Override
    public Void visit(Relation relation) {
      getRelations().put(relation.getId(), relation);
      return null;
    }
  };

  public void add(OsmObject osmObject) {
    osmObject.accept(addVisitor);
  }

  public boolean contains(long identity) {
    return get(identity) != null;
  }

  public OsmObject get(long identity) {
    OsmObject object = getNodes().get(identity);
    if (object == null) {
      object = getWays().get(identity);
      if (object == null) {
        object = getRelations().get(identity);
      }
    }
    return object;
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

  public Map<Long, Node> getNodes() {
    return nodes;
  }

  public void setNodes(Map<Long, Node> nodes) {
    this.nodes = nodes;
  }

  public Map<Long, Way> getWays() {
    return ways;
  }

  public void setWays(Map<Long, Way> ways) {
    this.ways = ways;
  }

  public Map<Long, Relation> getRelations() {
    return relations;
  }

  public void setRelations(Map<Long, Relation> relations) {
    this.relations = relations;
  }


  /**
   * @param filter returns true if instance is to be removed from results
   * @return
   */
  public Collection<OsmObject> filter(OsmObjectVisitor<Boolean> filter) {
    return filter(gatherAllOsmObjects(), filter);

  }
/**
   * @param filter returns true if instance is to be removed from results
   * @return
   */
  public Collection<OsmObject> filter(Collection<OsmObject> input, OsmObjectVisitor<Boolean> filter) {
    List<OsmObject> response = new ArrayList<OsmObject>(input);
    for (Iterator<OsmObject> iterator = response.iterator(); iterator.hasNext(); ) {
      OsmObject object = iterator.next();
      if (object.accept(filter)) {
        iterator.remove();
      }
    }
    return response;
  }


  public Set<OsmObject> gatherAllOsmObjects() {
    Set<OsmObject> objects = new HashSet<OsmObject>(getWays().size() + getRelations().size() + getNodes().size());
    objects.addAll(getWays().values());
    objects.addAll(getRelations().values());
    objects.addAll(getNodes().values());
    return objects;
  }

}
