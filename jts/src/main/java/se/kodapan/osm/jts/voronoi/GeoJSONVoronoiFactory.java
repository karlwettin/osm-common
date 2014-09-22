package se.kodapan.osm.jts.voronoi;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import org.json.JSONException;
import org.json.JSONObject;
import se.kodapan.geojson.Feature;
import se.kodapan.geojson.FeatureCollection;
import se.kodapan.geojson.GeometryCollection;
import se.kodapan.geojson.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author kalle
 * @since 2014-09-21 09:56
 */
public class GeoJSONVoronoiFactory<ClassType> extends AbstractVoronoiFactory<ClassType> {

  private FeatureCollection root;

  public FeatureCollection getRoot() {
    return root;
  }

  public void factory(Map<ClassType, List<Polygon>> voronoi) throws JSONException {

    root = new FeatureCollection();

    for (Map.Entry<ClassType, List<Polygon>> entry : voronoi.entrySet()) {

      Feature classFeature = new Feature();
      root.getFeatures().add(classFeature);
      classFeature.setProperties(new JSONObject());
      classFeature.getProperties().put("class", entry.getKey());

      GeometryCollection classFeatureMembers = new GeometryCollection();
      classFeature.setGeometry(classFeatureMembers);

      for (Polygon polygon : entry.getValue()) {

        se.kodapan.geojson.Polygon voronoiPolygon = new se.kodapan.geojson.Polygon();
        classFeatureMembers.getGeometries().add(voronoiPolygon);

        for (Coordinate coordinate : polygon.getExteriorRing().getCoordinates()) {
          voronoiPolygon.getHull().add(new Point(coordinate.x, coordinate.y));
        }

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {

          List<Point> hole = new ArrayList<Point>();
          for (Coordinate coordinate : polygon.getInteriorRingN(i).getCoordinates()) {
            hole.add(new Point(coordinate.x, coordinate.y));
          }
          voronoiPolygon.getHoles().add(hole);

        }
      }
    }

  }

}
