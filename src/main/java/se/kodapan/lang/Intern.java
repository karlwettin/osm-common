package se.kodapan.lang;

import java.io.Serializable;

/**
 * @author kalle
 * @since 2013-05-04 17:37
 */
public interface Intern<T> extends Serializable {
  public T intern(T object);
}
