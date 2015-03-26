package se.kodapan.osm.city;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.PojoRoot;
import se.kodapan.osm.parser.xml.instantiated.InstantiatedOsmXmlParser;
import se.kodapan.osm.util.distance.ArcDistance;
import se.kodapan.osm.xml.OsmXmlWriter;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kalle
 * @since 2015-01-12 03:47
 */
public class Grid {

  public static void main(String[] args) throws Exception {

  }

  private Cell[][] columns;

  private double south;
  private double west;
  private double north;
  private double east;


  private double gridColumns;
  private double gridRows;

  private int squarePixelsWidth;
  private int squarePixelsHeight;

  private double longitudeDegreesDistancePerPixel;
  private double latitudeDegreesDistancePerPixel;

  private double longitudeMetersDistancePerPixel;
  private double latitudeMetersDistancePerPixel;

  private double kilometersHeight;
  private double kilometersWidth;

  private int cellSquareSizeMeters;

  public Grid(double south, double west, double north, double east, int imageWidth, int imageHeight, int cellSquareSizeMeters) {
    this.south = south;
    this.west = west;
    this.north = north;
    this.east = east;

    this.cellSquareSizeMeters = cellSquareSizeMeters;

    latitudeDegreesDistancePerPixel = (north - south) / imageHeight;
    longitudeDegreesDistancePerPixel = (east - west) / imageWidth;

    ArcDistance arcDistance = new ArcDistance();
    kilometersHeight = arcDistance.calculate(south, west, north, west);
    kilometersWidth = arcDistance.calculate(south, west, south, east);

    longitudeMetersDistancePerPixel = imageWidth / kilometersWidth;
    latitudeMetersDistancePerPixel = imageHeight / kilometersHeight;


    gridColumns = (kilometersWidth * 1000) / cellSquareSizeMeters;
    gridRows = (kilometersHeight * 1000) / cellSquareSizeMeters;

    squarePixelsWidth = (int) ((double) imageWidth / gridColumns);
    squarePixelsHeight = (int) ((double) imageHeight / gridRows);

    columns = new Cell[(int) gridColumns][];
    for (int x = 0; x < columns.length; x++) {
      columns[x] = new Cell[(int) gridRows];
      for (int y = 0; y < columns[x].length; y++) {
        columns[x][y] = new Cell(x, y);
      }
    }
  }

  public void writeOsmXML(File file) throws Exception {
    Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
    writeOsmXML(writer);
    writer.close();
  }

  public void writeOsmXML(Writer writer) throws Exception {
    DecimalFormat df = new DecimalFormat("#.##");
    long id = -1;
    OsmXmlWriter xml = new OsmXmlWriter(writer);
    for (int x = 0; x < columns.length; x++) {
      for (int y = 0; y < columns[x].length; y++) {
        Cell cell = getCell(x, y);
        if (cell.getInstance() != null) {
          Way way = new Way(id--);
          if (cell.getClassification() != null) {
            way.setTag("class", cell.getClassification());
          }
          way.addNode(new Node(id--, cell.getInstance().getSouth(), cell.getInstance().getWest()));
          way.addNode(new Node(id--, cell.getInstance().getNorth(), cell.getInstance().getWest()));
          way.addNode(new Node(id--, cell.getInstance().getNorth(), cell.getInstance().getEast()));
          way.addNode(new Node(id--, cell.getInstance().getSouth(), cell.getInstance().getEast()));
          way.addNode(new Node(id--, cell.getInstance().getSouth(), cell.getInstance().getWest()));
          way.setTag("type", "bbox");
          for (int i = 0; i < cell.getInstance().getHistogramPercent().length; i++) {
            way.setTag("color:" + i + ":percent", df.format(100d * cell.getInstance().getHistogramPercent()[i]));
          }
          xml.write(way);
          for (Node node : way.getNodes()) {
            xml.write(node);
          }
        }
      }
    }
    xml.close();
  }

  public Map<Instance, String> loadTrainingData(InstanceFactory instanceFactory) throws Exception {


    PojoRoot root = new PojoRoot();
    InstantiatedOsmXmlParser parser = InstantiatedOsmXmlParser.newInstance();
    parser.setRoot(root);
    parser.parse(new FileInputStream("trainingdata-malmo.osm.xml"));

    Map<Instance, String> instances = new HashMap<Instance, String>(root.getWays().size());
    for (Way way : root.getWays().values()) {

      String classification = way.getTag("class");
      if (classification != null) {

        double south = way.getNodes().get(0).getLatitude();
        double west = way.getNodes().get(0).getLongitude();
        double north = way.getNodes().get(1).getLatitude();
        double east = way.getNodes().get(2).getLongitude();

        instances.put(instanceFactory.newInstance(south, west, north, east), classification);
      }

    }

    return instances;

  }

  public double getCellSouth(int x, int y) {
    return north - (latitudeDegreesDistancePerPixel * squarePixelsHeight * y);
  }

  public double getCellNorth(int x, int y) {
    return getCellSouth(x, y) + (latitudeDegreesDistancePerPixel * squarePixelsHeight);
  }

  public double getCellWest(int x, int y) {
    return east - (longitudeDegreesDistancePerPixel * squarePixelsWidth * x);
  }

  public double getCellEast(int x, int y) {
    return getCellWest(x, y) + (longitudeDegreesDistancePerPixel * squarePixelsWidth);
  }

  public int getWidth() {
    return (int) gridColumns;
  }

  public int getHeight() {
    return (int) gridRows;
  }

  public Cell getCellByCoordinate(double longitude, double latitude) {
    return getCell(
        getGridX(getImageX(longitude)), getGridY(getImageY(latitude))
    );
  }

  public Cell getCellByImagePosition(int x, int y) {
    return getCell(
        getGridX(x), getGridY(y)
    );
  }

  public Cell getCell(int x, int y) {
    return columns[x][y];
  }

  public int getGridX(int imageX) {
    return imageX / squarePixelsWidth;
  }

  public int getGridY(int imageY) {
    return imageY / squarePixelsHeight;
  }

  public int getImageX(double longitude) {
    return (int) ((longitude - west) / longitudeDegreesDistancePerPixel);
  }

  public int getImageY(double latitude) {
    return (int) ((north - latitude) / latitudeDegreesDistancePerPixel);
  }

  public double getLatitudeY(int y) {
    return south + (latitudeDegreesDistancePerPixel * y);
  }

  public double getLongitudeX(int x) {
    return west + (longitudeDegreesDistancePerPixel * x);
  }

  public Cell[][] getColumns() {
    return columns;
  }

  public double getSouth() {
    return south;
  }

  public double getWest() {
    return west;
  }

  public double getNorth() {
    return north;
  }

  public double getEast() {
    return east;
  }

  public double getGridColumns() {
    return gridColumns;
  }

  public double getGridRows() {
    return gridRows;
  }

  public int getSquarePixelsWidth() {
    return squarePixelsWidth;
  }

  public int getSquarePixelsHeight() {
    return squarePixelsHeight;
  }

  public double getLongitudeDegreesDistancePerPixel() {
    return longitudeDegreesDistancePerPixel;
  }

  public double getLatitudeDegreesDistancePerPixel() {
    return latitudeDegreesDistancePerPixel;
  }

  public double getLongitudeMetersDistancePerPixel() {
    return longitudeMetersDistancePerPixel;
  }

  public double getLatitudeMetersDistancePerPixel() {
    return latitudeMetersDistancePerPixel;
  }

  public double getKilometersHeight() {
    return kilometersHeight;
  }

  public double getKilometersWidth() {
    return kilometersWidth;
  }

  public int getCellSquareSizeMeters() {
    return cellSquareSizeMeters;
  }
}
