package se.kodapan.osm.geo.distance;

import se.kodapan.osm.domain.OsmObject;
import se.kodapan.osm.domain.OsmObjectVisitor;

/**
 * @author kalle
 * @since 2013-07-29 15:36
 */
public interface OsmObjectDistance {

  public abstract double calculate(OsmObject a, OsmObject b);
}
