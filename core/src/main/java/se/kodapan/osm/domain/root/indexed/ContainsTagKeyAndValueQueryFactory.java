package se.kodapan.osm.domain.root.indexed;

/**
 * Created by kalle on 10/20/13.
 */
public abstract class ContainsTagKeyAndValueQueryFactory<Query> extends QueryFactory<Query>{

  private String key;
  private String value;

  public String getKey() {
    return key;
  }

  public ContainsTagKeyAndValueQueryFactory<Query> setKey(String key) {
    this.key = key;
    return this;
  }

  public String getValue() {
    return value;
  }

  public ContainsTagKeyAndValueQueryFactory<Query> setValue(String value) {
    this.value = value;
    return this;
  }

}
