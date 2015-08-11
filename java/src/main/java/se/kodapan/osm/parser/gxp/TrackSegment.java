package se.kodapan.osm.parser.gxp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2015-08-11 18:55
 */
public class TrackSegment {

  private List<TrackPoint> trackPoints = new ArrayList<TrackPoint>();

  public List<TrackPoint> getTrackPoints() {
    return trackPoints;
  }

  public void setTrackPoints(List<TrackPoint> trackPoints) {
    this.trackPoints = trackPoints;
  }
}
