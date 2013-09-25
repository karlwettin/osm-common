package se.kodapan.osm.services.nominatim;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.OsmObject;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2013-07-27 21:12
 */
public class NominatimJsonResponseParser {

  private JSONParser parser = new JSONParser();
  private Root root = new Root();

  public List<Result> parse(NominatimQueryBuilder nominatimQueryBuilder) throws Exception {
    return parse(nominatimQueryBuilder.setFormat("json").build());
  }

  public List<Result> parse(String nominatimJsonResponse) throws Exception {

    JSONArray jsonResults = (JSONArray) parser.parse(nominatimJsonResponse);
    List<Result> results = new ArrayList<Result>(jsonResults.size());

    for (int i = 0; i < jsonResults.size(); i++) {
      JSONObject jsonResult = (JSONObject) jsonResults.get(i);

      Object osm_type = jsonResult.get("osm_type");

      OsmObject object;
      if ("node".equals(osm_type)) {
        Node node = new Node();
        node.setLatitude((parseJsonDoubleValue(jsonResult, "lat")));
        node.setLongitude((parseJsonDoubleValue(jsonResult, "lon")));
        object = node;
      } else if ("way".equals(osm_type)) {
        Way way = new Way();
        object = way;
      } else if ("relation".equals(osm_type)) {
        Relation relation = new Relation();
        object = relation;
      } else {
        throw new RuntimeException("Unknown osm_type: " + osm_type);
      }

      object.setId(parseJsonDoubleValue(jsonResult, "osm_id").longValue());
      object.setTag(
          (String) jsonResult.get("class"),
          (String) jsonResult.get("type"));

      Result result = new Result(jsonResult, null);
      OsmObject existing = root.get(object.getId());
      result.setObject(existing != null ? existing : object);
      results.add(result);
      root.add(object);

      System.currentTimeMillis();
    }


    return results;

  }

  public Root getRoot() {
    return root;
  }

  public void setRoot(Root root) {
    this.root = root;
  }

  public static class Result {
    private JSONObject jsonObject;
    private double importance;
    private OsmObject object;

    public Result(JSONObject jsonObject, OsmObject object) {
      this.jsonObject = jsonObject;
      this.object = object;
      this.importance = parseJsonDoubleValue(jsonObject, "importance");
    }

    public double getImportance() {
      return importance;
    }

    public void setImportance(double importance) {
      this.importance = importance;
    }

    public OsmObject getObject() {
      return object;
    }

    public void setObject(OsmObject object) {
      this.object = object;
    }

    public JSONObject getJsonObject() {
      return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
      this.jsonObject = jsonObject;
    }

    @Override
    public String toString() {
      return "Result{" +
          "importance=" + importance +
          ", object=" + object +
          ", jsonObject=" + jsonObject +
          '}';
    }
  }

  /**
   * @param jsonObject instance of String, Number or null
   * @param key
   * @return
   */
  public static Double parseJsonDoubleValue(JSONObject jsonObject, String key) {
    Object value = jsonObject.get(key);
    if (value == null) {
      return null;
    } else {
      if (value instanceof Number) {
        return ((Number) value).doubleValue();
      }
      try {
        return Double.parseDouble(value.toString());
      } catch (NumberFormatException e) {
        e.printStackTrace();
        return null;
      }
    }
  }

}
