package se.kodapan.osm.domain;

import java.io.Serializable;

/**
 * @author kalle
 * @since 2013-05-01 21:06
 */
public class RelationMembership implements Serializable {

  private static final long serialVersionUID = 1l;

  private Relation relation;
  private OsmObject object;

  /** todo intern in domain! */
  private String role;

  public Relation getRelation() {
    return relation;
  }

  public void setRelation(Relation relation) {
    this.relation = relation;
  }

  public OsmObject getObject() {
    return object;
  }

  public void setObject(OsmObject object) {
    this.object = object;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return "RelationMembership{" +
        "role='" + role + '\'' +
        ", relation=" + relation +
        ", object.id=" + (object != null ? object.getId() : "null") +
        '}';
  }
}
