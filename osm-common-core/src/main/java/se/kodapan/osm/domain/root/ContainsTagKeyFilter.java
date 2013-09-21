package se.kodapan.osm.domain.root;

import se.kodapan.osm.domain.*;

/**
* @author kalle
* @since 2013-07-27 21:42
*/
public class ContainsTagKeyFilter extends AbstractTagFilter {

  private String key;

  public ContainsTagKeyFilter(String key) {
    if (key == null) {
      throw new NullPointerException("Parameter key can not be null!");
    }
    this.key = key;
  }

  @Override
  public Boolean visit(OsmObject tagged) {
    return tagged.getTags() == null || !tagged.getTags().containsKey(key);
  }

}
