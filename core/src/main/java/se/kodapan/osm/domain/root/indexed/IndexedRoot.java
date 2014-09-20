package se.kodapan.osm.domain.root.indexed;

import se.kodapan.osm.domain.Node;
import se.kodapan.osm.domain.OsmObject;
import se.kodapan.osm.domain.Relation;
import se.kodapan.osm.domain.Way;
import se.kodapan.osm.domain.root.AbstractRoot;
import se.kodapan.osm.domain.root.Root;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

/**
 * Created by kalle on 10/19/13.
 */
public abstract class IndexedRoot<Query> extends AbstractRoot {

  public static Class<IndexedRoot> factoryClass;

  public static IndexedRoot newInstance(Root decorated) {
    return newInstance(decorated, null);
  }

  /**
   * @return a new instance depending on underlying OS. E.g. Android or Java.
   */
  public static IndexedRoot newInstance(Root decorated, File directory) {
    synchronized (IndexedRoot.class) {
      if (factoryClass == null) {
        try {
          factoryClass = (Class<IndexedRoot>) Class.forName(IndexedRoot.class.getName() + "Impl");
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    }
    try {
      if (directory != null) {
        return factoryClass.getConstructor(Root.class, File.class).newInstance(decorated, directory);
      } else {
        return factoryClass.getConstructor(Root.class).newInstance(decorated);
      }
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract QueryFactories<Query> getQueryFactories();

  private Root decorated;

  protected IndexedRoot(Root decorated) {
    this.decorated = decorated;
  }

  @Override
  public Enumerator<Node> enumerateNodes() {
    return decorated.enumerateNodes();
  }

  @Override
  public Enumerator<Way> enumerateWays() {
    return decorated.enumerateWays();
  }

  @Override
  public Enumerator<Relation> enumerateRelations() {
    return decorated.enumerateRelations();
  }

  @Override
  public Node getNode(long identity) {
    return decorated.getNode(identity);
  }

  @Override
  public Way getWay(long identity) {
    return decorated.getWay(identity);
  }

  @Override
  public Relation getRelation(long identity) {
    return decorated.getRelation(identity);
  }


  @Override
  /** Should also remove this object and update all affected objects in index. */
  public abstract Set<OsmObject> remove(OsmObject osmObject);

  @Override
  /** Should also update this object and all affected objects in index. */
  public abstract void add(OsmObject osmObject);


  /**
   * Reconstructs the whole index from scratch.
   *
   * @param numberOfThreads number of threads used to write to index
   */
  public abstract void reconstruct(int numberOfThreads) throws IOException;

  public abstract void open() throws IOException;

  public abstract void close() throws IOException;

  /**
   * Commits and changes to the index and makes it available for queries.
   *
   * @throws IOException
   */
  public abstract void commit() throws IOException;

  public abstract Map<OsmObject, Float> search(Query query) throws IOException;

  public Root getDecorated() {
    return decorated;
  }

  public void setDecorated(Root decorated) {
    this.decorated = decorated;
  }
}
