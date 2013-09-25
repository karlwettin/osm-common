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

  private Map<Long, Node> nodes = new HashMap<Long, Node>();
  private Map<Long, Way> ways = new HashMap<Long, Way>();
  private Map<Long, Relation> relations = new HashMap<Long, Relation>();

  private class AddVisitor implements OsmObjectVisitor<Void>,Serializable {

    private static final long serialVersionUID = 1l;

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

  }

  private OsmObjectVisitor<Void> addVisitor = new AddVisitor();

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

  public List<Node> findNodeByLatitudeAndLongitude(double latitude, double longitude) {
    List<Node> nodes = new ArrayList<Node>(100);
    for (Node node : getNodes().values()) {
      if (node.getLatitude() == latitude && node.getLongitude() == longitude) {
        nodes.add(node);
      }
    }
    return nodes;
  }

  /**
   * todo this loop get really slow where there is hundreds of thousands of coordinates and as many calls to the method... index?
   * todo if an index then it needs to be up to date when node values change. that requires keeping track of root from osmobjects....
   * todo and what if the object is added to multiple roots then?
   * todo so an index is probably not possible.
   */
  public Node findFirstNodeByLatitudeAndLongitude(double latitude, double longitude) {
    for (Node node : getNodes().values()) {
      if (node.getLatitude() == latitude && node.getLongitude() == longitude) {
        return node;
      }
    }
    return null;
  }


}
