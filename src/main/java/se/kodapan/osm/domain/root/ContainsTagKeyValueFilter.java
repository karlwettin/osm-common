package se.kodapan.osm.domain.root;

import se.kodapan.osm.domain.*;

/**
* @author kalle
* @since 2013-07-27 21:42
*/
public class ContainsTagKeyValueFilter extends AbstractTagFilter {

  private String key;
  private String value;

  public ContainsTagKeyValueFilter(String key, String value) {

    if (key == null) {
      throw new NullPointerException("Parameter key can not be null! Looking for ContainsTagValueFilter?");
    }
    if (value == null){
      throw new NullPointerException("Parameter value can not be null! Looking for ContainsTagKeyFilter?");
    }

    this.key = key;
    this.value = value;
  }

  @Override
  public Boolean visit(OsmObject tagged) {
    return !value.equals(tagged.getTag(key));
  }

}
