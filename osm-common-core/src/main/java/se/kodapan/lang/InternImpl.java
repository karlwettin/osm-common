package se.kodapan.lang;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kalle
 * @since 2013-05-04 17:38
 */
public class InternImpl<T> implements Intern<T>, Serializable {
  private static final long serialVersionUID = 1l;
  private Map<T, T> map = new HashMap<T, T>();

  @Override
  public T intern(T object) {
    T interned = map.get(object);
    if (interned == null) {
      map.put(object, object);
      interned = object;
    }
    return interned;
  }

  @Override
  public String toString() {
    return "InternImpl{" +
        "map.size=" + map.size() +
        '}';
  }
}
