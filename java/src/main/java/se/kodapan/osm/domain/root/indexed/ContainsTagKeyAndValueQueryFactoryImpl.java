package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Created by kalle on 10/20/13.
 */
public class ContainsTagKeyAndValueQueryFactoryImpl extends ContainsTagKeyAndValueQueryFactory<Query> {

  @Override
  public Query build() {
    if (getKey() == null || getValue() == null) {
      throw new NullPointerException();
    }
    return new TermQuery(new Term("tag.key_and_value", getKey()+ "=" + getValue()));
  }

}
