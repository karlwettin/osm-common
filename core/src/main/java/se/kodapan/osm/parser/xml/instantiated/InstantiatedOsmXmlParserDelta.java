package se.kodapan.osm.parser.xml.instantiated;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;

import java.util.HashSet;
import java.util.Set;

/**
 * @author kalle
 * @since 2013-05-02 03:20
 */
public class InstantiatedOsmXmlParserDelta {

  private Set<Node> createdNodes = new HashSet<>();
  private Set<Node> modifiedNodes = new HashSet<>();
  private Set<Node> deletedNodes = new HashSet<>();

  private Set<Way> createdWays = new HashSet<>();
  private Set<Way> modifiedWays = new HashSet<>();
  private Set<Way> deletedWays = new HashSet<>();

  private Set<Relation> createdRelations = new HashSet<>();
  private Set<Relation> modifiedRelations = new HashSet<>();
  private Set<Relation> deletedRelations = new HashSet<>();


  public Set<Node> getCreatedNodes() {
    return createdNodes;
  }

  public void setCreatedNodes(Set<Node> createdNodes) {
    this.createdNodes = createdNodes;
  }

  public Set<Node> getModifiedNodes() {
    return modifiedNodes;
  }

  public void setModifiedNodes(Set<Node> modifiedNodes) {
    this.modifiedNodes = modifiedNodes;
  }

  public Set<Node> getDeletedNodes() {
    return deletedNodes;
  }

  public void setDeletedNodes(Set<Node> deletedNodes) {
    this.deletedNodes = deletedNodes;
  }

  public Set<Way> getCreatedWays() {
    return createdWays;
  }

  public void setCreatedWays(Set<Way> createdWays) {
    this.createdWays = createdWays;
  }

  public Set<Way> getModifiedWays() {
    return modifiedWays;
  }

  public void setModifiedWays(Set<Way> modifiedWays) {
    this.modifiedWays = modifiedWays;
  }

  public Set<Way> getDeletedWays() {
    return deletedWays;
  }

  public void setDeletedWays(Set<Way> deletedWays) {
    this.deletedWays = deletedWays;
  }

  public Set<Relation> getCreatedRelations() {
    return createdRelations;
  }

  public void setCreatedRelations(Set<Relation> createdRelations) {
    this.createdRelations = createdRelations;
  }

  public Set<Relation> getModifiedRelations() {
    return modifiedRelations;
  }

  public void setModifiedRelations(Set<Relation> modifiedRelations) {
    this.modifiedRelations = modifiedRelations;
  }

  public Set<Relation> getDeletedRelations() {
    return deletedRelations;
  }

  public void setDeletedRelations(Set<Relation> deletedRelations) {
    this.deletedRelations = deletedRelations;
  }
}
