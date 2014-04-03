package se.kodapan.osm.jts;

import com.vividsolutions.jts.geom.Coordinate;
import junit.framework.TestCase;
import se.kodapan.osm.domain.Node;

import java.util.List;

/**
 * @author kalle
 * @since 2014-04-03 05:15
 */
public class TestLineInterpolation extends TestCase {

  public void test() throws Exception {

    LineInterpolation lineInterpolation = new LineInterpolation();

    Node a = new Node();
    a.setLatitude(56.677526);
    a.setLongitude(12.8592571);

    Node b = new Node();
    b.setLatitude(56.6775432);
    b.setLongitude(12.8592488);

    double maximumKilometersPerCoordinate = 0.0010;

    List<Coordinate> coordinates = lineInterpolation.interpolate(maximumKilometersPerCoordinate, b, a);

    assertEquals(3, coordinates.size());

    assertEquals(b.getX(), coordinates.get(0).x);
    assertEquals(b.getY(), coordinates.get(0).y);

    assertEquals(12.85925295, coordinates.get(1).x);
    assertEquals(56.6775346, coordinates.get(1).y);

    assertEquals(a.getX(), coordinates.get(2).x);
    assertEquals(a.getY(), coordinates.get(2).y);


    System.currentTimeMillis();


  }

}
