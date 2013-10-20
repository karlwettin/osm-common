package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Created by kalle on 10/20/13.
 */
public class ContainsTagKeyQueryFactory {

  private String key;

  public String getKey() {
    return key;
  }

  public ContainsTagKeyQueryFactory setKey(String key) {
    this.key = key;
    return this;
  }

  public Query build() {
    if (key == null) {
      throw new NullPointerException();
    }
    return new TermQuery(new Term("tag.key", key));
  }

}
