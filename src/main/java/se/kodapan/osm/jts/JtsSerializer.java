package se.kodapan.osm.jts;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2013-09-09 6:06 PM
 */
public class JtsSerializer {

  private GeometryFactory geometryFactory;

  public JtsSerializer(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void writePolygon(Polygon polygon, ObjectOutputStream out) throws IOException {
    for (Coordinate coordinate : polygon.getCoordinates()) {
      out.writeBoolean(true);
      out.writeDouble(coordinate.x);
      out.writeDouble(coordinate.y);
    }
    out.writeBoolean(false);

    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      out.writeBoolean(true);
      LineString inner = polygon.getInteriorRingN(i);
      for (Coordinate coordinate : inner.getCoordinates()) {
        out.writeBoolean(true);
        out.writeDouble(coordinate.x);
        out.writeDouble(coordinate.y);
      }
      out.writeBoolean(false);

    }
    out.writeBoolean(false);
  }

  public Polygon readPolygon(ObjectInputStream in) throws IOException {


    List<Coordinate> shellCoordinates = new ArrayList<Coordinate>();
    while (in.readBoolean()) {
      shellCoordinates.add(new Coordinate(in.readDouble(), in.readDouble()));
    }
    shellCoordinates.add(shellCoordinates.get(0));
    CoordinateSequence shellPoints = new CoordinateArraySequence(shellCoordinates.toArray(new Coordinate[shellCoordinates.size()]));
    LinearRing shell = new LinearRing(shellPoints, geometryFactory);


    List<LinearRing> holes = new ArrayList<LinearRing>();
    while (in.readBoolean()) {
      List<Coordinate> holeCoordinates = new ArrayList<Coordinate>();

      while (in.readBoolean()) {
        holeCoordinates.add(new Coordinate(in.readDouble(), in.readDouble()));
      }
      holeCoordinates.add(holeCoordinates.get(0));

      CoordinateSequence holePoints = new CoordinateArraySequence(shellCoordinates.toArray(new Coordinate[holeCoordinates.size()]));
      LinearRing hole = new LinearRing(holePoints, geometryFactory);
      holes.add(hole);

    }

    return new Polygon(shell, holes.isEmpty() ? null : holes.toArray(new LinearRing[holes.size()]), geometryFactory);
  }
}
