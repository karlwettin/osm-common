package se.kodapan.osm.parser.gxp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2015-08-11 19:17
 */
public class Gpx {

  private List<WayPoint> wayPoints = new ArrayList<WayPoint>();
  private List<Track> tracks = new ArrayList<Track>();

  public List<WayPoint> getWayPoints() {
    return wayPoints;
  }

  public void setWayPoints(List<WayPoint> wayPoints) {
    this.wayPoints = wayPoints;
  }

  public List<Track> getTracks() {
    return tracks;
  }

  public void setTracks(List<Track> tracks) {
    this.tracks = tracks;
  }
}
