package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Created by kalle on 10/20/13.
 */
public class ContainsTagValueQueryFactoryImpl extends ContainsTagValueQueryFactory<Query> {

  public Query build() {
    if (getKey() == null) {
      throw new NullPointerException();
    }
    return new TermQuery(new Term("tag.key", getKey()));
  }

}
