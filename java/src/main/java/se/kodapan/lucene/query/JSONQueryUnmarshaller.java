package se.kodapan.lucene.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author kalle
 * @since 2014-09-10 18:20
 */
public class JSONQueryUnmarshaller {


  public Query parse(JSONObject jsonQuery) throws JSONException {

    String type = jsonQuery.getString("type");
    if ("boolean query".equalsIgnoreCase(type)) {

      BooleanQuery booleanQuery = new BooleanQuery();

      JSONArray jsonClauses = jsonQuery.getJSONArray("clauses");

      for (int i = 0; i < jsonClauses.length(); i++) {

        JSONObject jsonClause = jsonClauses.getJSONObject(i);

        BooleanClause.Occur occur;
        String jsonOccur = jsonClause.getString("occur");
        if (jsonOccur.equalsIgnoreCase("must")) {
          occur = BooleanClause.Occur.MUST;
        } else if (jsonOccur.equalsIgnoreCase("should")) {
          occur = BooleanClause.Occur.SHOULD;
        } else if (jsonOccur.equalsIgnoreCase("must not")) {
          occur = BooleanClause.Occur.MUST_NOT;
        } else {
          throw new IllegalArgumentException("Expected occurs with value 'must', 'should' or 'must not', but was '" + jsonOccur + "'.");
        }

        booleanQuery.add(new BooleanClause(parse(jsonClause.getJSONObject("query")), occur));

      }

      return booleanQuery;

    } else if ("match all documents".equalsIgnoreCase(type)) {

      return new MatchAllDocsQuery();

    } else if ("term".equalsIgnoreCase(type)) {

      return new TermQuery(new Term(jsonQuery.getString("field"), jsonQuery.getString("value")));

    } else if ("coordinate envelope".equalsIgnoreCase(type)) {

      return new CoordinateEnvelopeQueryFactory()
          .setLatitudeField(jsonQuery.getString("latitudeField"))
          .setLongitudeField(jsonQuery.getString("longitudeField"))

          .setSouth(jsonQuery.getDouble("southLatitude"))
          .setWest(jsonQuery.getDouble("westLongitude"))
          .setNorth(jsonQuery.getDouble("northLatitude"))
          .setEast(jsonQuery.getDouble("eastLongitude"))
          .build();

    } else if ("coordinate circle envelope".equalsIgnoreCase(type)) {


      return new CoordinateCircleEnvelopeQueryFactory()
          .setLatitudeField(jsonQuery.getString("latitudeField"))
          .setLongitudeField(jsonQuery.getString("longitudeField"))

          .setCentroidLatitude(jsonQuery.getDouble("centroidLatitude"))
          .setCentroidLongitude(jsonQuery.getDouble("centroidLongitude"))
          .setRadiusKilometers(jsonQuery.getDouble("radiusKilometers"))
          .build();

    } else if ("integer range".equalsIgnoreCase(type)) {

      int minimum = !jsonQuery.isNull("minimum") ? jsonQuery.getInt("minimum") : Integer.MIN_VALUE;
      boolean includeMinimum = !jsonQuery.has("includeMinimum") || jsonQuery.getBoolean("includeMinimum");

      int maximum = !jsonQuery.isNull("maximum") ? jsonQuery.getInt("maximum") : Integer.MAX_VALUE;
      boolean includeMaximum = !jsonQuery.has("includeMaximum") || jsonQuery.getBoolean("includeMaximum");

      return NumericRangeQuery.newIntRange(jsonQuery.getString("field"), minimum, maximum, includeMinimum, includeMaximum);

    } else if ("long range".equalsIgnoreCase(type)) {

      long minimum = !jsonQuery.isNull("minimum") ? jsonQuery.getLong("minimum") : Long.MIN_VALUE;
      boolean includeMinimum = !jsonQuery.has("includeMinimum") || jsonQuery.getBoolean("includeMinimum");

      long maximum = !jsonQuery.isNull("maximum") ? jsonQuery.getLong("maximum") : Long.MAX_VALUE;
      boolean includeMaximum = !jsonQuery.has("includeMaximum") || jsonQuery.getBoolean("includeMaximum");

      return NumericRangeQuery.newLongRange(jsonQuery.getString("field"), minimum, maximum, includeMinimum, includeMaximum);

    } else if ("float range".equalsIgnoreCase(type)) {

      float minimum = !jsonQuery.isNull("minimum") ? (float) jsonQuery.getDouble("minimum") : Float.MIN_VALUE;
      boolean includeMinimum = !jsonQuery.has("includeMinimum") || jsonQuery.getBoolean("includeMinimum");

      float maximum = !jsonQuery.isNull("maximum") ? (float) jsonQuery.getDouble("maximum") : Float.MAX_VALUE;
      boolean includeMaximum = !jsonQuery.has("includeMaximum") || jsonQuery.getBoolean("includeMaximum");

      return NumericRangeQuery.newFloatRange(jsonQuery.getString("field"), minimum, maximum, includeMinimum, includeMaximum);

    } else if ("double range".equalsIgnoreCase(type)) {

      double minimum = !jsonQuery.isNull("minimum") ? jsonQuery.getDouble("minimum") : Double.MIN_VALUE;
      boolean includeMinimum = !jsonQuery.has("includeMinimum") || jsonQuery.getBoolean("includeMinimum");

      double maximum = !jsonQuery.isNull("maximum") ? jsonQuery.getDouble("maximum") : Double.MAX_VALUE;
      boolean includeMaximum = !jsonQuery.has("includeMaximum") || jsonQuery.getBoolean("includeMaximum");

      return NumericRangeQuery.newDoubleRange(jsonQuery.getString("field"), minimum, maximum, includeMinimum, includeMaximum);

    } else {
      throw new IllegalArgumentException("Unsupported query type '" + type + "'.");
    }

  }


}
