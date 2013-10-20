package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Created by kalle on 10/20/13.
 */
public class ContainsTagKeyAndValueQueryFactory {

  private String key;
  private String value;

  public String getKey() {
    return key;
  }

  public ContainsTagKeyAndValueQueryFactory setKey(String key) {
    this.key = key;
    return this;
  }

  public String getValue() {
    return value;
  }

  public ContainsTagKeyAndValueQueryFactory setValue(String value) {
    this.value = value;
    return this;
  }

  public Query build() {
    if (key == null || value == null) {
      throw new NullPointerException();
    }
    return new TermQuery(new Term("tag.key_and_value", key + "=" + value));
  }

}
