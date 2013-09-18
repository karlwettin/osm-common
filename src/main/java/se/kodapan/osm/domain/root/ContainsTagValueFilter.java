package se.kodapan.osm.domain.root;

import se.kodapan.osm.domain.*;

/**
 * Returns object containing any tag key matching a given tag value.
 *
 * @author kalle
 * @since 2013-07-27 21:42
 */
public class ContainsTagValueFilter extends AbstractTagFilter {

  private String value;

  public ContainsTagValueFilter(String value) {
    if (value == null) {
      throw new NullPointerException("Parameter value can not be null!");
    }
    this.value = value;
  }

  @Override
  public Boolean visit(OsmObject tagged) {
    return tagged.getTags() == null || !tagged.getTags().values().contains(value);
  }

}
