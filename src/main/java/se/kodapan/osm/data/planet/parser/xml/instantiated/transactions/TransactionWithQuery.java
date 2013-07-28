package se.kodapan.osm.data.planet.parser.xml.instantiated.transactions;

import se.kodapan.osm.domain.root.Root;

import java.io.Serializable;

/**
 * @author kalle
 * @since 2013-05-04 21:54
 */
public interface TransactionWithQuery<R> extends Serializable {

  public R executeOn(Root root);

}
