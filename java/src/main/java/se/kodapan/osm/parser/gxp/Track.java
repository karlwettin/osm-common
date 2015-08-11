package se.kodapan.osm.parser.gxp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2015-08-11 18:56
 */
public class Track {

  private List<TrackSegment> trackSegments = new ArrayList<TrackSegment>();

  public TrackPoint findTrackPointClosestToTimestamp(long timestamp) {
    long smallestDiff = Long.MAX_VALUE;
    TrackPoint closestTrackPoint = null;
    for (TrackSegment trackSegment : trackSegments) {
      for (TrackPoint trackPoint : trackSegment.getTrackPoints()) {
        long diff = Math.max(timestamp, trackPoint.getTimestamp()) - Math.min(timestamp, trackPoint.getTimestamp());
        if (closestTrackPoint == null
            || diff < smallestDiff) {
          smallestDiff = diff;
          closestTrackPoint = trackPoint;
        }
      }
    }
    return closestTrackPoint;
  }

  public List<TrackSegment> getTrackSegments() {
    return trackSegments;
  }

  public void setTrackSegments(List<TrackSegment> trackSegments) {
    this.trackSegments = trackSegments;
  }
}
