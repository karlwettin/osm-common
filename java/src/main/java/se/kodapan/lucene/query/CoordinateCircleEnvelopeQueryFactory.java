package se.kodapan.lucene.query;

import org.apache.lucene.search.Query;

/**
 * Constructs an envelope from a centroid and radius
 * by creating a circle and putting an envelope around it.
 *
 * @author kalle
 * @since 2014-09-09 14:40
 */
public class CoordinateCircleEnvelopeQueryFactory {

  private String latitudeField;
  private String longitudeField;


  private int circumferenceResolution = 8;

  private double radiusKilometers;

  private double centroidLatitude;
  private double centroidLongitude;

  public CoordinateCircleEnvelopeQueryFactory setCircumferenceResolution(int circumferenceResolution) {
    this.circumferenceResolution = circumferenceResolution;
    return this;
  }

  public CoordinateCircleEnvelopeQueryFactory setLatitudeField(String latitudeField) {
    this.latitudeField = latitudeField;
    return this;
  }

  public CoordinateCircleEnvelopeQueryFactory setLongitudeField(String longitudeField) {
    this.longitudeField = longitudeField;
    return this;
  }

  public CoordinateCircleEnvelopeQueryFactory setRadiusKilometers(double radiusKilometers) {
    this.radiusKilometers = radiusKilometers;
    return this;
  }

  public CoordinateCircleEnvelopeQueryFactory setCentroidLatitude(double centroidLatitude) {
    this.centroidLatitude = centroidLatitude;
    return this;
  }

  public CoordinateCircleEnvelopeQueryFactory setCentroidLongitude(double centroidLongitude) {
    this.centroidLongitude = centroidLongitude;
    return this;
  }

  public String getLatitudeField() {
    return latitudeField;
  }

  public String getLongitudeField() {
    return longitudeField;
  }

  public int getCircumferenceResolution() {
    return circumferenceResolution;
  }

  public double getRadiusKilometers() {
    return radiusKilometers;
  }

  public double getCentroidLatitude() {
    return centroidLatitude;
  }

  public double getCentroidLongitude() {
    return centroidLongitude;
  }



  public Query build() {

    double south = Double.MAX_VALUE;
    double west = Double.MAX_VALUE;
    double east = Double.MIN_VALUE;
    double north = Double.MIN_VALUE;

    double radiusLatitude = (radiusKilometers / 6378.8d) * (180 / Math.PI);
    double radiusLongitude = radiusLatitude / Math.cos(centroidLatitude * (Math.PI / 180));

    int step = (int) (360d / (double) circumferenceResolution);
//    for (int i = 0; i <= 361; i+= step) {
    for (int i = 0; i < 360; i += step) {
      double a = i * (Math.PI / 180);
      double latitude = centroidLatitude + (radiusLatitude * Math.sin(a));
      double longitude = centroidLongitude + (radiusLongitude * Math.cos(a));

      if (latitude < south) {
        south = latitude;
      }
      if (latitude > north) {
        north = latitude;
      }
      if (longitude < west) {
        west = longitude;
      }
      if (longitude > east) {
        east = longitude;
      }

    }

    return new CoordinateEnvelopeQueryFactory()
        .setLatitudeField(latitudeField)
        .setLongitudeField(longitudeField)
        .setSouth(south)
        .setWest(west)
        .setNorth(north)
        .setEast(east)
        .build();

  }

}
