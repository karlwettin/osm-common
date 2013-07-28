package se.kodapan.osm.data.planet.parser.xml.instantiated;

import java.util.HashSet;
import java.util.Set;

/**
 * @author kalle
 * @since 2013-05-02 03:20
 */
public class InstantiatedOsmXmlParserDelta {

  private Set<Long> createdNodeIdentities = new HashSet<Long>();
  private Set<Long> modifiedNodeIdentities = new HashSet<Long>();
  private Set<Long> deletedNodeIdentities = new HashSet<Long>();

  private Set<Long> createdWayIdentities = new HashSet<Long>();
  private Set<Long> modifiedWayIdentities = new HashSet<Long>();
  private Set<Long> deletedWayIdentities = new HashSet<Long>();

  private Set<Long> createdRelationIdentities = new HashSet<Long>();
  private Set<Long> modifiedRelationIdentities = new HashSet<Long>();
  private Set<Long> deletedRelationIdentities = new HashSet<Long>();

  public Set<Long> getCreatedNodeIdentities() {
    return createdNodeIdentities;
  }

  public void setCreatedNodeIdentities(Set<Long> createdNodeIdentities) {
    this.createdNodeIdentities = createdNodeIdentities;
  }

  public Set<Long> getModifiedNodeIdentities() {
    return modifiedNodeIdentities;
  }

  public void setModifiedNodeIdentities(Set<Long> modifiedNodeIdentities) {
    this.modifiedNodeIdentities = modifiedNodeIdentities;
  }

  public Set<Long> getDeletedNodeIdentities() {
    return deletedNodeIdentities;
  }

  public void setDeletedNodeIdentities(Set<Long> deletedNodeIdentities) {
    this.deletedNodeIdentities = deletedNodeIdentities;
  }

  public Set<Long> getCreatedWayIdentities() {
    return createdWayIdentities;
  }

  public void setCreatedWayIdentities(Set<Long> createdWayIdentities) {
    this.createdWayIdentities = createdWayIdentities;
  }

  public Set<Long> getModifiedWayIdentities() {
    return modifiedWayIdentities;
  }

  public void setModifiedWayIdentities(Set<Long> modifiedWayIdentities) {
    this.modifiedWayIdentities = modifiedWayIdentities;
  }

  public Set<Long> getDeletedWayIdentities() {
    return deletedWayIdentities;
  }

  public void setDeletedWayIdentities(Set<Long> deletedWayIdentities) {
    this.deletedWayIdentities = deletedWayIdentities;
  }

  public Set<Long> getCreatedRelationIdentities() {
    return createdRelationIdentities;
  }

  public void setCreatedRelationIdentities(Set<Long> createdRelationIdentities) {
    this.createdRelationIdentities = createdRelationIdentities;
  }

  public Set<Long> getModifiedRelationIdentities() {
    return modifiedRelationIdentities;
  }

  public void setModifiedRelationIdentities(Set<Long> modifiedRelationIdentities) {
    this.modifiedRelationIdentities = modifiedRelationIdentities;
  }

  public Set<Long> getDeletedRelationIdentities() {
    return deletedRelationIdentities;
  }

  public void setDeletedRelationIdentities(Set<Long> deletedRelationIdentities) {
    this.deletedRelationIdentities = deletedRelationIdentities;
  }
}
