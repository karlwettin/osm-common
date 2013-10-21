package se.kodapan.osm.domain.root.indexed;

/**
 * Created by kalle on 10/20/13.
 */
public abstract class ContainsTagKeyQueryFactory<Query> extends QueryFactory<Query>{

  private String key;

  public String getKey() {
    return key;
  }

  public ContainsTagKeyQueryFactory<Query> setKey(String key) {
    this.key = key;
    return this;
  }

}
