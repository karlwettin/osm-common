package se.kodapan.osm.domain;

import java.io.Serializable;
import java.util.*;

/**
 * @author kalle
 * @since 2013-05-01 15:42
 */
public abstract class OsmObject implements Serializable {

  private static final long serialVersionUID = 1l;

  public abstract <R> R accept(OsmObjectVisitor<R> visitor);

  /**
   * if true, then this object has not been loaded, it's just a referenced object
   */
  private boolean loaded;

  private Long id;

  private Map<String, String> attributes;

  private Integer version;
  private Long changeset;
  private Long uid;
  private String user;
  private boolean visible;
  private Long timestamp;

  private Map<String, String> tags;

  private List<RelationMembership> relationMemberships;

  public void addRelationMembership(RelationMembership member) {
    if (relationMemberships == null) {
      relationMemberships = new ArrayList<RelationMembership>(5);
    } else {
      // don't add membership to the same object twice
      for (RelationMembership relationMembership : relationMemberships) {
        if (relationMembership.getRelation().equals(member.getRelation())) {
          return;
        }
      }
    }

    relationMemberships.add(member);
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public List<RelationMembership> getRelationMemberships() {
    return relationMemberships;
  }

  public void setRelationMemberships(List<RelationMembership> relationMemberships) {
    this.relationMemberships = relationMemberships;
  }

  public String getAttribute(String key) {
    if (attributes == null) {
      return null;
    }
    return attributes.get(key);
  }

  public String setAttribute(String key, String value) {
    if (attributes == null) {
      attributes = new HashMap<String, String>(5);
    }
    return attributes.put(key, value);
  }

  public String getTag(String key) {
    if (tags == null) {
      return null;
    }
    return tags.get(key);
  }

  public String setTag(String key, String value) {
    if (tags == null) {
      tags = new LinkedHashMap<String, String>(5);
    }
    return tags.put(key, value);
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Long getChangeset() {
    return changeset;
  }

  public void setChangeset(Long changeset) {
    this.changeset = changeset;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public Long getUid() {
    return uid;
  }

  public void setUid(Long uid) {
    this.uid = uid;
  }

  public boolean isLoaded() {
    return loaded;
  }

  public void setLoaded(boolean loaded) {
    this.loaded = loaded;
  }

  @Override
  public String toString() {
    return "OsmObject{" +
        "loaded=" + loaded +
        ", id=" + id +
        ", attributes=" + attributes +
        ", version=" + version +
        ", changeset=" + changeset +
        ", uid=" + uid +
        ", user='" + user + '\'' +
        ", visible=" + visible +
        ", timestamp=" + timestamp +
        ", tags=" + tags +
        ", relationMemberships=" + relationMemberships +
        '}';
  }
}
