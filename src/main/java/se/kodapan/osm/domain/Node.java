package se.kodapan.osm.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2013-05-01 15:42
 */
public class Node extends OsmObject implements Serializable {

  private static final long serialVersionUID = 1l;

  @Override
  public <R> R accept(OsmObjectVisitor<R> visitor) {
    return visitor.visit(this);
  }

  private double latitude;
  private double longitude;

  private List<Way> waysMemberships;

  public void addWayMembership(Way way) {
    if (waysMemberships == null) {
      waysMemberships = new ArrayList<Way>(5);
    }
    waysMemberships.add(way);
  }

  public List<Way> getWaysMemberships() {
    return waysMemberships;
  }

  public void setWaysMemberships(List<Way> waysMemberships) {
    this.waysMemberships = waysMemberships;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  @Override
  public String toString() {
    return "Node{" +
        super.toString() +
        "latitude=" + latitude +
        ", longitude=" + longitude +
        ", waysMemberships.size=" + (waysMemberships == null ? "null" : waysMemberships.size()) +
        '}';
  }
}
