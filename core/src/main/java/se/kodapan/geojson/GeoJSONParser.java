package se.kodapan.geojson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2015-03-26 00:09
 */
public class GeoJSONParser {

  public static Feature parseFeature(JSONObject json) throws JSONException {
    Feature feature = new Feature();
    feature.setGeometry(parseGeometry(json.getJSONObject("geometry")));
    if (json.has("properties")) {
      feature.setProperties(json.getJSONObject("properties"));
    }
    return feature;
  }

  public static FeatureCollection parseFeatureCollection(JSONObject json) throws JSONException {
    FeatureCollection featureCollection = new FeatureCollection();

    JSONArray features = json.getJSONArray("features");
    for (int i = 0; i < features.length(); i++) {
      JSONObject feature = features.getJSONObject(i);
      featureCollection.getFeatures().add(parseFeature(feature));
    }

    return featureCollection;
  }

  public static GeoJSONGeometry parseGeometry(JSONObject json) throws JSONException {

    String type = json.getString("type");
    if ("LineString".equals(type)) {
      return parseLineString(json);
    } else if ("MultiLineString".equals(type)) {
      return parseMultiLineString(json);
    } else if ("Point".equals(type)) {
      return parsePoint(json);
    } else if ("MultiPoint".equals(type)) {
      return parseMultiPoint(json);
    } else if ("Polygon".equals(type)) {
      return parsePolygon(json);
    } else if ("MultiPolygon".equals(type)) {
      return parseMultiPolygon(json);
    } else if ("GeometryCollection".equals(type)) {
      return parseGeometryCollection(json);
    } else {
      throw new RuntimeException("Unsupported type: " + type);
    }

  }

  public static Point parsePoint(JSONObject json) throws JSONException {
    Point point = new Point();

    JSONArray coordinates = json.getJSONArray("coordinates");
    point.setLongitude(coordinates.getDouble(0));
    point.setLatitude(coordinates.getDouble(1));

    return point;
  }

  public static MultiPoint parseMultiPoint(JSONObject json) throws JSONException {
    MultiPoint multiPoint = new MultiPoint();

    JSONArray points = json.getJSONArray("coordinates");
    for (int i = 0; i < points.length(); i++) {

      Point point = new Point();
      JSONArray coordinates = points.getJSONArray(i);

      point.setLongitude(coordinates.getDouble(0));
      point.setLatitude(coordinates.getDouble(1));

      multiPoint.getCoordinates().add(point);

    }


    return multiPoint;
  }

  public static LineString parseLineString(JSONObject json) throws JSONException {
    LineString lineString = new LineString();


    JSONArray points = json.getJSONArray("coordinates");
    for (int i = 0; i < points.length(); i++) {

      Point point = new Point();
      JSONArray coordinates = points.getJSONArray(i);

      point.setLongitude(coordinates.getDouble(0));
      point.setLatitude(coordinates.getDouble(1));

      lineString.getCoordinates().add(point);

    }


    return lineString;
  }

  public static MultiLineString parseMultiLineString(JSONObject json) throws JSONException {
    MultiLineString multiLineString = new MultiLineString();

    JSONArray lineStrings = json.getJSONArray("coordinates");
    for (int l = 0; l < lineStrings.length(); l++) {

      LineString lineString = new LineString();

      JSONArray points = json.getJSONArray("coordinates");
      for (int i = 0; i < points.length(); i++) {

        Point point = new Point();
        JSONArray coordinates = points.getJSONArray(i);

        point.setLongitude(coordinates.getDouble(0));
        point.setLatitude(coordinates.getDouble(1));

        lineString.getCoordinates().add(point);

      }

      multiLineString.getLineStrings().add(lineString);

    }


    return multiLineString;
  }

  public static Polygon parsePolygon(JSONObject json) throws JSONException {
    Polygon polygon = new Polygon();

    JSONArray coordinates = json.getJSONArray("coordinates");

    for (int i = 0; i < coordinates.length(); i++) {

      JSONArray jsonPoints = coordinates.getJSONArray(i);

      List<Point> points = new ArrayList<Point>(jsonPoints.length());

      for (int p = 0; p < jsonPoints.length(); p++) {
        JSONArray jsonPoint = jsonPoints.getJSONArray(p);
        Point point = new Point();
        point.setLongitude(jsonPoint.getDouble(0));
        point.setLatitude(jsonPoint.getDouble(1));
        points.add(point);
      }

      if (i == 0) {
        polygon.setHull(points);
      } else {
        polygon.getHoles().add(points);
      }

    }

    return polygon;
  }

  public static MultiPolygon parseMultiPolygon(JSONObject json) throws JSONException {
    MultiPolygon multiPolygon = new MultiPolygon();


    JSONArray polygons = json.getJSONArray("coordinates");

    for (int pi = 0; pi < polygons.length(); pi++) {

      Polygon polygon = new Polygon();

      JSONArray coordinates = polygons.getJSONArray(pi);

      for (int i = 0; i < coordinates.length(); i++) {

        JSONArray jsonPoints = coordinates.getJSONArray(i);

        List<Point> points = new ArrayList<Point>(jsonPoints.length());

        for (int p = 0; p < jsonPoints.length(); p++) {
          JSONArray jsonPoint = jsonPoints.getJSONArray(p);
          Point point = new Point();
          point.setLongitude(jsonPoint.getDouble(0));
          point.setLatitude(jsonPoint.getDouble(1));
          points.add(point);
        }

        if (i == 0) {
          polygon.setHull(points);
        } else {
          polygon.getHoles().add(points);
        }

      }

      multiPolygon.getPolygons().add(polygon);

    }

    return multiPolygon;
  }

  public static GeometryCollection parseGeometryCollection(JSONObject json) throws JSONException {
    GeometryCollection geometryCollection = new GeometryCollection();

    JSONArray geometries = json.getJSONArray("geometries");
    for (int i=0; i<geometries.length(); i++) {
      geometryCollection.getGeometries().add(parseGeometry(geometries.getJSONObject(i)));
    }

    return geometryCollection;
  }

}
