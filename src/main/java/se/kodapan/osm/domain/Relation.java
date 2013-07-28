package se.kodapan.osm.domain;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2013-05-01 15:42
 */
public class Relation extends OsmObject implements Serializable {

  private static final long serialVersionUID = 1l;

  @Override
  public <R> R accept(OsmObjectVisitor<R> visitor) {
    return visitor.visit(this);
  }

  private List<RelationMembership> members;


  public void addMember(RelationMembership member) {
    if (members == null) {
      members = new ArrayList<RelationMembership>(50);
    }
    members.add(member);
  }


  public List<RelationMembership> getMembers() {
    return members;
  }

  public void setMembers(List<RelationMembership> members) {
    this.members = members;
  }

  @Override
  public String toString() {
    return "Relation{" +
        super.toString() +
        "members=" + members +
        '}';
  }
}
