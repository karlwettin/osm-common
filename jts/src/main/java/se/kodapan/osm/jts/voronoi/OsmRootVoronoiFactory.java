package se.kodapan.osm.jts.voronoi;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.RelationMembership;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.PojoRoot;
import se.kodapan.osm.domain.root.Root;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author kalle
 * @since 2014-09-21 09:57
 */
public abstract class OsmRootVoronoiFactory<ClassType> extends AbstractVoronoiFactory<ClassType> {

  private long identity = -1;

  private PojoRoot root;

  public PojoRoot getRoot() {
    return root;
  }

  public void factory(Map<ClassType, List<Polygon>> voronoi) throws IOException {

    root = new PojoRoot();

    for (Map.Entry<ClassType, List<Polygon>> entry : voronoi.entrySet()) {
      Relation classTypeInstanceMultiPolygon = new Relation();
      classTypeInstanceMultiPolygon.setId(identity--);
      root.add(classTypeInstanceMultiPolygon);
      classTypeInstanceMultiPolygon.setTag("type", "multipolygon");


      for (Polygon geometry : entry.getValue()) {

        RelationMembership classMembership;

        if (geometry instanceof Polygon) {
          Polygon polygon = (Polygon) geometry;
          if (polygon.getNumInteriorRing() == 0) {

            Way way = new Way();
            way.setId(identity--);
            root.add(way);

            addNodesToWay(root, entry, geometry, polygon, way);
            setWay(way, entry.getKey(), entry.getValue(), geometry);

            classMembership = new RelationMembership();
            classMembership.setRole("outer");
            classMembership.setRelation(classTypeInstanceMultiPolygon);
            classMembership.setObject(way);
            classTypeInstanceMultiPolygon.addMember(classMembership);
            way.addRelationMembership(classMembership);

          } else {
            Relation geometryMultiPolygon = new Relation();
            geometryMultiPolygon.setTag("type", "multipolygon");

            geometryMultiPolygon.setId(identity--);
            root.add(geometryMultiPolygon);

            classMembership = new RelationMembership();
            classMembership.setRole("outer");
            classMembership.setRelation(classTypeInstanceMultiPolygon);
            classMembership.setObject(geometryMultiPolygon);
            classTypeInstanceMultiPolygon.addMember(classMembership);
            geometryMultiPolygon.addRelationMembership(classMembership);


            RelationMembership geometryMultiPolygonMembership = new RelationMembership();
            geometryMultiPolygonMembership.setRole("outer");
            geometryMultiPolygonMembership.setRelation(geometryMultiPolygon);

            Way outerWay = new Way();
            outerWay.setId(identity--);
            root.add(outerWay);
            geometryMultiPolygon.addMember(geometryMultiPolygonMembership);
            outerWay.addRelationMembership(geometryMultiPolygonMembership);
            geometryMultiPolygonMembership.setObject(outerWay);

            addNodesToWay(root, entry, geometry, polygon, outerWay);
            setWay(outerWay, entry.getKey(), entry.getValue(), geometry);

            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
              RelationMembership inner = new RelationMembership();
              inner.setRole("inner");
              inner.setRelation(geometryMultiPolygon);
              Way innerWay = new Way();
              innerWay.setId(identity--);
              root.add(innerWay);
              geometryMultiPolygon.addMember(inner);
              innerWay.addRelationMembership(inner);
              inner.setObject(innerWay);

              addNodesToWay(root, entry, geometry, polygon, innerWay);
              setWay(innerWay, entry.getKey(), entry.getValue(), geometry);

            }

            setGeometryMultiPolygon(geometryMultiPolygon, entry.getKey(), entry.getValue(), geometry);

          }
        } else {
          throw new RuntimeException();
        }

        setClassTypeInstanceMultiPolygon(classTypeInstanceMultiPolygon, entry.getKey(), entry.getValue());
      }
    }

  }

  private void addNodesToWay(Root root, Map.Entry<ClassType, List<Polygon>> entry, Polygon geometry, Polygon polygon, Way innerWay) {
    for (Coordinate coordinate : polygon.getExteriorRing().getCoordinates()) {
      Node node = null; // todo see method.. slow.. root.findFirstNodeByLatitudeAndLongitude(coordinate.y, coordinate.x);
      if (node == null) {
        node = new Node();
        node.setId(identity--);
        node.setLongitude(coordinate.x);
        node.setLatitude(coordinate.y);
        root.add(node);
        setNode(node, entry.getKey(), entry.getValue(), geometry, coordinate);
      }
      node.addWayMembership(innerWay);
      innerWay.addNode(node);
    }
  }

  public abstract void setNode(Node node, ClassType type, List<Polygon> geometries, Polygon geometry, Coordinate coordinate);

  public abstract void setWay(Way way, ClassType type, List<Polygon> geometries, Polygon geometry);

  public abstract void setClassTypeInstanceMultiPolygon(Relation relation, ClassType type, List<Polygon> geometries);

  public abstract void setGeometryMultiPolygon(Relation relation, ClassType type, List<Polygon> geometries, Polygon geometry);


}
