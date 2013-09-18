package se.kodapan.osm.jts;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.RelationMembership;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.Root;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Takes a number of points per class
 * and create polygons per class
 * joined together on class where adjacent with each other
 *
 * @author kalle
 * @since 2013-03-27 02:49
 */
public class AdjacentClassVoronoiClusterer<ClassType> {

  private Logger log = LoggerFactory.getLogger(AdjacentClassVoronoiClusterer.class);

  private GeometryFactory factory;

  public AdjacentClassVoronoiClusterer(GeometryFactory factory) {
    this.factory = factory;
  }

  private int numberOfThreads = 16;

  private boolean bound = false;
  private double boundsLatitudeSouth;
  private double boundsLongitudeWest;
  private double boundsLatitudeNorth;
  private double boundsLongitudeEast;

  public void setBounds(double latitudeSouth, double longitudeWest, double latitudeNorth, double longitudeEast) {
    bound = true;
    this.boundsLatitudeSouth = latitudeSouth;
    this.boundsLongitudeWest = longitudeWest;
    this.boundsLatitudeNorth = latitudeNorth;
    this.boundsLongitudeEast = longitudeEast;
  }


  private boolean isBound(Coordinate coordinate) {
    return isBound(coordinate.y, coordinate.x);
  }

  private boolean isBound(double latitude, double longitude) {
    return !bound
        || latitude >= this.boundsLatitudeSouth
        && latitude <= this.boundsLatitudeNorth
        && longitude >= this.boundsLongitudeWest
        && longitude <= this.boundsLongitudeEast;
  }

  private Map<ClassType, Set<Coordinate>> coordinatesByClass = new HashMap<ClassType, Set<Coordinate>>();


  public Map<ClassType, List<Polygon>> build() throws InterruptedException {

    log.debug("Selecting unique voronoi sites...");


    // if the same coordinate is shared by multiple classes, then don't use it!
    // this occurs in intersections, etc.
    // todo
    // a better solution would be to add two points just a few millimeter away from each other in the correct direction
    // but how does one know what direction is the correct one?


    int counter = 0;

    Set<Coordinate> coordinatesSeen = new HashSet<Coordinate>();
    Map<Coordinate, ClassType> classByCoordinate = new HashMap<Coordinate, ClassType>();

    Map<ClassType, Set<Coordinate>> classCoordinates = new HashMap<ClassType, Set<Coordinate>>();
    for (Map.Entry<ClassType, Set<Coordinate>> entry : this.coordinatesByClass.entrySet()) {
      Set<Coordinate> values = new HashSet<Coordinate>(entry.getValue().size());
      for (Coordinate coordinate : entry.getValue()) {
        values.add(coordinate);
        counter++;
      }
      classCoordinates.put(entry.getKey(), values);
    }

    log.debug(counter + " coordinates in training data... Keeping only unique...");


    List<Coordinate> voronoiSites = new ArrayList<Coordinate>();


    long timestamp = System.currentTimeMillis();
    int debug = 0;
    for (Map.Entry<ClassType, Set<Coordinate>> classCoordinatesEntry : classCoordinates.entrySet()) {
      for (Coordinate coordinate : classCoordinatesEntry.getValue()) {

        if (coordinatesSeen.add(coordinate)) {

          boolean useCoordinate = true;
          if (false) {
            for (Map.Entry<ClassType, Set<Coordinate>> classCoordinatesEntry2 : classCoordinates.entrySet()) {
              if (classCoordinatesEntry2.getKey().equals(classCoordinatesEntry.getKey())) {
                continue;
              }
              if (classCoordinatesEntry2.getValue().contains(coordinate)) {
                useCoordinate = false;
                break;
              }
            }
          }
          if (useCoordinate) {
            voronoiSites.add(coordinate);
            classByCoordinate.put(coordinate, classCoordinatesEntry.getKey());
          }
        }
        counter--;
        debug++;
      }

      if (debug > 100) {
        debug = 0;
        if (System.currentTimeMillis() - timestamp > 5000) {
          log.info(counter + " coordinates left to process...");
        }
      }

    }


    log.info("Create voronoi regions from " + voronoiSites.size() + " sites...");


    VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
    voronoiBuilder.setSites(voronoiSites);
    if (bound) {
      voronoiBuilder.setClipEnvelope(new Envelope(boundsLongitudeWest, boundsLongitudeEast, boundsLatitudeSouth, boundsLatitudeNorth));
    }

    GeometryCollection voronoiRegions = (GeometryCollection) voronoiBuilder.getDiagram(factory);


    log.debug("Mapping voronoi regions to classes...");
    // order is messed up and compared to input and not all sites might have created a polygon...

    Map<ClassType, Set<Geometry>> voronoiRegionsByClass = new HashMap<ClassType, Set<Geometry>>();

    for (int i = 0; i < voronoiRegions.getNumGeometries(); i++) {
      Geometry geometry = voronoiRegions.getGeometryN(i);
      Coordinate userData = (Coordinate) geometry.getUserData();
      Coordinate coordinate = new Coordinate(userData.x, userData.y);
      ClassType _class = classByCoordinate.get(coordinate);
      Set<Geometry> regions = voronoiRegionsByClass.get(_class);
      if (regions == null) {
        regions = new HashSet<Geometry>();
        voronoiRegionsByClass.put(_class, regions);
      }
      regions.add(geometry);
    }


    log.debug("Merge adjacent class regions to single polygon");
    // merge adjacent class regions to single polygon.
    // first create a list of all polygons with same class that share at least one coordinate.
    Map<ClassType, Set<Set<Geometry>>> adjacentRegionsByClass = new HashMap<ClassType, Set<Set<Geometry>>>();

    for (Map.Entry<ClassType, Set<Geometry>> entry : voronoiRegionsByClass.entrySet()) {

      Set<Set<Geometry>> regions = adjacentRegionsByClass.get(entry.getKey());
      if (regions == null) {
        regions = new HashSet<Set<Geometry>>();
        adjacentRegionsByClass.put(entry.getKey(), regions);
      }
      for (Geometry geometry : entry.getValue()) {
        Set<Geometry> set = new HashSet<Geometry>();
        set.add(geometry);
        regions.add(set);
      }
    }


    final Map<ClassType, List<List<Geometry>>> mergedAdjacentRegionsByClass = new HashMap<ClassType, List<List<Geometry>>>();
    {
      final Queue<Map.Entry<ClassType, Set<Set<Geometry>>>> queue = new ConcurrentLinkedQueue<Map.Entry<ClassType, Set<Set<Geometry>>>>(adjacentRegionsByClass.entrySet());

      Thread[] threads = new Thread[numberOfThreads];
      for (int i = 0; i < threads.length; i++) {
        Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
            Map.Entry<ClassType, Set<Set<Geometry>>> entry;
            while ((entry = queue.poll()) != null) {
              long timer = System.currentTimeMillis();

              log.debug("Class " + entry.getKey() + " containing " + entry.getValue().size() + " voronoi polygons merge");

              List<List<Geometry>> currentRegionsState = new ArrayList<List<Geometry>>(entry.getValue().size());
              for (Set<Geometry> set : entry.getValue()) {
                currentRegionsState.add(new ArrayList<Geometry>(set));
              }

              // run until no more adjacent regions (points that equal in different regions) found
              // todo this is really slow when there are a lot of voronoi points (as many geometries) for this class
              // but it works.

              for (Iterator<List<Geometry>> regionsIterator = currentRegionsState.iterator(); regionsIterator.hasNext(); ) {
                List<Geometry> regions = regionsIterator.next();

                for (Iterator<Geometry> regionIterator = regions.iterator(); regionIterator.hasNext(); ) {
                  Geometry region = regionIterator.next();

                  boolean merged = false;

                  for (Coordinate point : region.getCoordinates()) {


                    for (List<Geometry> testRegions : currentRegionsState) {
                      if (testRegions == regions) {
                        continue;
                      }
                      for (Geometry testRegion : testRegions) {
                        for (Coordinate testPoint : testRegion.getCoordinates()) {

                          if (point.equals(testPoint)) {
                            testRegions.addAll(regions);
                            regionsIterator.remove();
                            merged = true;
                            break;
                          }

                        }
                        if (merged) {
                          break;
                        }
                      }
                      if (merged) {
                        break;
                      }
                    }
                    if (merged) {
                      break;
                    }

                  }
                  if (merged) {
                    break;
                  }


                }

              }


              mergedAdjacentRegionsByClass.put(entry.getKey(), currentRegionsState);

              timer = System.currentTimeMillis() - timer;

              log.debug("Class " + entry.getKey() + " containing " + entry.getValue().size() + " voronoi polygons merged to " + currentRegionsState.size() + " adjacent polygon groups in " + timer + " ms");

            }
          }
        });
        thread.setName("Voronoi polygon merge thread #" + i);
        thread.setDaemon(true);
        thread.start();
        threads[i] = thread;
      }

      for (Thread thread : threads) {
        thread.join();
      }

    }


    // merged... now union. i.e. make multiple adjacent polygons a single polygon using geometry.buffer(0)

    log.info("Union polygons per class...");

    final Map<ClassType, List<Polygon>> unionPolygonsByClass = new HashMap<ClassType, List<Polygon>>();

    for (Map.Entry<ClassType, List<List<Geometry>>> entry : mergedAdjacentRegionsByClass.entrySet()) {

      long timer = System.currentTimeMillis();

      int count = 0;
      for (List<Geometry> regions : entry.getValue()) {
        count += regions.size();
      }
      log.debug("Union " + entry.getKey() + " with " + entry.getValue().size() + " regions, or a total of " + count + " polygons.");

      final List<Polygon> unions = new ArrayList<Polygon>();

      final Queue<List<Geometry>> queue = new ConcurrentLinkedQueue<List<Geometry>>(entry.getValue());

      Thread[] threads = new Thread[numberOfThreads];
      for (int i = 0; i < threads.length; i++) {
        Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
            List<Geometry> region;
            while ((region = queue.poll()) != null) {

              List<Polygon> unionFactory = new ArrayList<Polygon>(region.size());

              for (Geometry polygon : region) {
                unionFactory.add((Polygon) polygon);
              }

              Geometry union = factory.buildGeometry(unionFactory).buffer(0);

              if (union instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon) union;
                for (int geometryIndex = 0; geometryIndex < multiPolygon.getNumGeometries(); geometryIndex++) {
                  Geometry geometry = multiPolygon.getGeometryN(geometryIndex);
                  if (geometry instanceof Polygon) {
                    unions.add((Polygon) geometry);
                  } else {
                    throw new RuntimeException("Not implemented! " + union.getClass().getName());
                  }
                }
              } else if (union instanceof Polygon) {
                unions.add((Polygon) union);

              } else {
                throw new RuntimeException("Not implemented! " + union.getClass().getName());
              }

            }
          }
        });
        thread.setName("Voronoi polygon union thread #" + i);
        thread.setDaemon(true);
        thread.start();
        threads[i] = thread;

      }
      for (Thread thread : threads) {
        thread.join();
      }

      timer = System.currentTimeMillis() - timer;
      log.debug("Union " + entry.getKey() + " in " + timer + " ms");

      unionPolygonsByClass.put(entry.getKey(), unions);

    }


    return unionPolygonsByClass;


  }

  public void addCoordinate(ClassType _class, Node node) {
    addCoordinate(_class, node.getLongitude(), node.getLatitude());
  }

  public void addCoordinate(ClassType _class, double longitude, double latitude) {
    addCoordinate(_class, new Coordinate(longitude, latitude));
  }

  public void addCoordinate(ClassType _class, Coordinate coordinate) {
    if (!isBound(coordinate)) {
      return;
    }
    Set<Coordinate> coordinates = coordinatesByClass.get(_class);
    if (coordinates == null) {
      coordinates = new HashSet<Coordinate>();
      coordinatesByClass.put(_class, coordinates);
    }
    coordinates.add(coordinate);
  }

  public GeometryFactory getFactory() {
    return factory;
  }

  public void setFactory(GeometryFactory factory) {
    this.factory = factory;
  }

  public Map<ClassType, Set<Coordinate>> getCoordinatesByClass() {
    return coordinatesByClass;
  }

  public void setCoordinatesByClass(Map<ClassType, Set<Coordinate>> coordinatesByClass) {
    this.coordinatesByClass = coordinatesByClass;
  }

  public abstract static class OsmRootFactory<ClassType> {


    private long identity = -1;

    public Root factory(Map<ClassType, List<Polygon>> voronoi) throws IOException {

      Root root = new Root();

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

      return root;
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

  public int countCoordinates() {
    int counter = 0;
    for (Set<Coordinate> coordinates : this.getCoordinatesByClass().values()) {
      counter += coordinates.size();
    }
    return counter;
  }


}
