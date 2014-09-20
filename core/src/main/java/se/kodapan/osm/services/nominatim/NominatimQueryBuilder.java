package se.kodapan.osm.services.nominatim;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author kalle
 * @since 2013-03-27 02:06
 */
public class NominatimQueryBuilder {

  private String baseURL = "http://nominatim.openstreetmap.org/search";

  private String query;
  private String format;

  private Integer limit;
  private List<String> countryCodes;

  public String build() throws UnsupportedEncodingException {

    StringBuilder url = new StringBuilder();
    url.append(baseURL);

    url.append("?");

    if (query != null) {
      url.append("q=");
      url.append(URLEncoder.encode(query, "UTF8").replaceAll("\\+", "%20"));
      url.append("&");
    }

    if (format != null) {
      url.append("format=");
      url.append(format);
      url.append("&");
    }
    if (limit != null) {
      url.append("limit=");
      url.append(String.valueOf(limit));
      url.append("&");
    }

    if (countryCodes != null && !countryCodes.isEmpty()) {
      url.append("countrycodes=");
      for (Iterator<String> it = countryCodes.iterator(); it.hasNext(); ) {
        url.append(it.next());
        if (it.hasNext()) {
          url.append(",");
        }
      }
      url.append("&");
    }

    return url.toString();

  }

  public String getBaseURL() {
    return baseURL;
  }

  public NominatimQueryBuilder setBaseURL(String baseURL) {
    this.baseURL = baseURL;
    return this;
  }

  public String getQuery() {
    return query;
  }

  public NominatimQueryBuilder setQuery(String query) {
    this.query = query;
    return this;
  }

  public String getFormat() {
    return format;
  }

  public NominatimQueryBuilder setFormat(String format) {
    this.format = format;
    return this;
  }

  public List<String> getCountryCodes() {
    return countryCodes;
  }

  public NominatimQueryBuilder setCountryCodes(List<String> countryCodes) {
    this.countryCodes = countryCodes;
    return this;
  }

  public NominatimQueryBuilder addCountryCode(String countryCode) {
    if (countryCodes == null) {
      countryCodes = new ArrayList<String>();
    }
    countryCodes.add(countryCode);
    return this;
  }

  public Integer getLimit() {
    return limit;
  }

  public NominatimQueryBuilder setLimit(Integer limit) {
    this.limit = limit;
    return this;
  }

}
