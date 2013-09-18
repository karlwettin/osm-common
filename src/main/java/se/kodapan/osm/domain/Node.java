package se.kodapan.osm.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @author Sebastian Pretzsch
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

  public double getY() {
    return getLatitude();
  }

  public void setY(double latitude) {
    setLatitude(latitude);
  }

  public double getX() {
    return getLongitude();
  }

  public void setX(double longitude) {
    setLongitude(longitude);
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

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	long temp;
	temp = Double.doubleToLongBits(latitude);
	result = prime * result + (int) (temp ^ (temp >>> 32));
	temp = Double.doubleToLongBits(longitude);
	result = prime * result + (int) (temp ^ (temp >>> 32));
	return result;
}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (Double.doubleToLongBits(latitude) != Double
				.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double
				.doubleToLongBits(other.longitude))
			return false;
		return true;
	}
  
  
  
}
