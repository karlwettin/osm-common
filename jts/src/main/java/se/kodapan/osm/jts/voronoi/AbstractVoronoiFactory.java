package se.kodapan.osm.jts.voronoi;

import com.vividsolutions.jts.geom.Polygon;
import se.kodapan.osm.domain.root.PojoRoot;

import java.util.List;
import java.util.Map;

/**
 * @author kalle
 * @since 2014-09-21 09:57
 */
public abstract class AbstractVoronoiFactory<ClassType> {

  public abstract void factory(Map<ClassType, List<Polygon>> voronoi) throws Exception;


}
