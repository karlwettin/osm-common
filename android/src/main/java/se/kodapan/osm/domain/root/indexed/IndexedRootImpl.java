package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.osm.domain.*;
import se.kodapan.osm.domain.root.Root;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * Implementation using Lucene 3.5.0.
 * <p/>
 * Created by kalle on 10/19/13.
 */
public class IndexedRootImpl extends IndexedRoot<Query> {

  private static Logger log = LoggerFactory.getLogger(IndexedRootImpl.class);

  private QueryFactories<Query> queryFactories = new QueryFactoriesImpl();

  @Override
  public QueryFactories<Query> getQueryFactories() {
    return queryFactories;
  }

  private File fileSystemDirectory;
  private Directory directory;
  private IndexWriter indexWriter;
  private SearcherManager searcherManager;

  private OsmObjectVisitor<Void> addVisitor = new AddVisitor();


  public IndexedRootImpl(Root decorated, File fileSystemDirectory) {
    super(decorated);
    this.fileSystemDirectory = fileSystemDirectory;
  }

  public IndexedRootImpl(Root decorated) {
    super(decorated);
  }

  private Analyzer analyzer = new KeywordAnalyzer();

  public Directory getDirectory() {
    return directory;
  }

  public void setDirectory(Directory directory) {
    if (open) {
      throw new RuntimeException("Need to close current Directory first.");
    }
    this.directory = directory;
  }

  @Override
  public void reconstruct(int numberOfThreads) throws IOException {
    final ConcurrentLinkedQueue<OsmObject> queue = new ConcurrentLinkedQueue<OsmObject>();
    Enumerator<Node> nodes = enumerateNodes();
    Node node;
    while ((node = nodes.next()) != null) {
      queue.add(node);
    }
    Enumerator<Way> ways = enumerateWays();
    Way way;
    while ((way = ways.next()) != null) {
      queue.add(way);
    }
    Enumerator<Relation> relations = enumerateRelations();
    Relation relation;
    while ((relation = relations.next()) != null) {
      queue.add(relation);
    }

    indexWriter.deleteAll();

    Thread[] threads = new Thread[numberOfThreads];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          OsmObject object;
          while ((object = queue.poll()) != null) {
            object.accept(indexVisitor);
          }
        }
      });
      threads[i].setName("Reconstruct IndexableRoot thread #" + i);
      threads[i].setDaemon(true);
      threads[i].start();
    }
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    commit();
  }

  private boolean open = false;

  @Override
  public void open() throws IOException {
    if (fileSystemDirectory == null) {
      directory = new RAMDirectory();
    } else {
      directory = new SimpleFSDirectory(fileSystemDirectory);
    }
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    SearcherWarmer warmer = null;
    ExecutorService es = null; // todo
    indexWriter = new IndexWriter(directory, config);
    searcherManager = new SearcherManager(indexWriter, true, warmer, es);
    open = true;
  }

  @Override
  public void close() throws IOException {
    searcherManager.close();
    indexWriter.close();
    directory.close();
    open = false;
  }

  private IndexVisitor indexVisitor = new IndexVisitor();

  private class IndexVisitor implements OsmObjectVisitor<Void>, Serializable {

    private static final long serialVersionUID = 1l;

    private void addObjectFields(OsmObject object, Document document) {
      if (object.getTags() != null) {
        for (Map.Entry<String, String> tag : object.getTags().entrySet()) {
          document.add(new Field("tag.key", tag.getKey(), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
          document.add(new Field("tag.value", tag.getValue(), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
          document.add(new Field("tag.key_and_value", tag.getKey() + "=" + tag.getValue(), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
        }
      }
    }

    public NumericField numericCoordinateFieldFactory(String name, double value) {
      NumericField field = new NumericField(name, 4, Field.Store.NO, true);
      field.setDoubleValue(value);
      return field;
    }

    @Override
    public Void visit(Node node) {
      Document document = new Document();
      document.add(new Field("class", "node", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
      document.add(new Field("node.identity", String.valueOf(node.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

      if (node.isLoaded()) {
        document.add(numericCoordinateFieldFactory("node.latitude", node.getLatitude()));
        document.add(numericCoordinateFieldFactory("node.longitude", node.getLongitude()));
      } else if (log.isInfoEnabled()) {
        log.info("Indexing node " + node.getId() + " which has not been loaded. Coordinates will not be searchable.");
      }


      addObjectFields(node, document);
      try {
        indexWriter.updateDocument(new Term("node.identity", String.valueOf(node.getId())), document);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      return null;
    }

    @Override
    public Void visit(Way way) {

      Document document = new Document();
      document.add(new Field("class", "way", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
      document.add(new Field("way.identity", String.valueOf(way.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

      if (way.getNodes() != null) {

        double southLatitude = 90d;
        double westLongitude = 180d;
        double northLatitude = -90d;
        double eastLongitude = -180d;

        boolean hasLoadedNodes = false;

        for (Node node : way.getNodes()) {
          if (!node.isLoaded()) {
            if (log.isDebugEnabled()) {
              log.debug("Skipping non loaded node " + node.getId() + " in way " + way.getId());
            }
            continue;
          }
          if (node.getLatitude() < southLatitude) {
            southLatitude = node.getLatitude();
          }
          if (node.getLatitude() > northLatitude) {
            northLatitude = node.getLatitude();
          }
          if (node.getLongitude() < westLongitude) {
            westLongitude = node.getLongitude();
          }
          if (node.getLongitude() > eastLongitude) {
            eastLongitude = node.getLongitude();
          }
          hasLoadedNodes = true;
        }

        if (hasLoadedNodes) {

          document.add(numericCoordinateFieldFactory("way.envelope.south_latitude", southLatitude));
          document.add(numericCoordinateFieldFactory("way.envelope.west_longitude", westLongitude));
          document.add(numericCoordinateFieldFactory("way.envelope.north_latitude", northLatitude));
          document.add(numericCoordinateFieldFactory("way.envelope.east_longitude", eastLongitude));

        } else if (log.isInfoEnabled()) {
          log.info("Indexing way " + way.getId() + " which contains nodes that are not loaded. Coordinates will not be searchable.");
        }

      }

      addObjectFields(way, document);
      try {
        indexWriter.updateDocument(new Term("way.identity", String.valueOf(way.getId())), document);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      return null;
    }

    @Override
    public Void visit(Relation relation) {
      Document document = new Document();
      document.add(new Field("class", "relation", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
      document.add(new Field("relation.identity", String.valueOf(relation.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

      // todo envelope

      addObjectFields(relation, document);


      try {
        indexWriter.updateDocument(new Term("relation.identity", String.valueOf(relation.getId())), document);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      return null;
    }
  }


  @Override
  public Set<OsmObject> remove(OsmObject object) {
    Set<OsmObject> affectedRelations = object.accept(removeVisitor);
    return affectedRelations;
  }

  private RemoveVisitor removeVisitor = new RemoveVisitor();

  private class RemoveVisitor implements OsmObjectVisitor<Set<OsmObject>>, Serializable {
    private static final long serialVersionUID = 1l;

    @Override
    public Set<OsmObject> visit(Node node) {
      Set<OsmObject> affectedObjects = getDecorated().remove(node);
      try {
        indexWriter.deleteDocuments(new Term("node.identity", String.valueOf(node.getId())));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      for (OsmObject object : affectedObjects) {
        object.accept(indexVisitor);
      }
      return affectedObjects;
    }

    @Override
    public Set<OsmObject> visit(Way way) {
      Set<OsmObject> affectedObjects = getDecorated().remove(way);
      try {
        indexWriter.deleteDocuments(new Term("way.identity", String.valueOf(way.getId())));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      for (OsmObject object : affectedObjects) {
        object.accept(indexVisitor);
      }
      return affectedObjects;
    }

    @Override
    public Set<OsmObject> visit(Relation relation) {
      Set<OsmObject> affectedObjects = getDecorated().remove(relation);
      try {
        indexWriter.deleteDocuments(new Term("relation.identity", String.valueOf(relation.getId())));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      for (OsmObject object : affectedObjects) {
        object.accept(indexVisitor);
      }
      return affectedObjects;
    }
  }


  private class AddVisitor implements OsmObjectVisitor<Void>, Serializable {


    private static final long serialVersionUID = 1l;


    @Override
    public Void visit(Node node) {
      getDecorated().add(node);
      node.accept(indexVisitor);
      return null;
    }

    @Override
    public Void visit(Way way) {
      getDecorated().add(way);
      way.accept(indexVisitor);
      return null;
    }

    @Override
    public Void visit(Relation relation) {
      getDecorated().add(relation);
      relation.accept(indexVisitor);
      return null;
    }

  }

  @Override
  public void add(OsmObject osmObject) {
    osmObject.accept(addVisitor);
  }

  @Override
  public void commit() throws IOException {
    indexWriter.commit();
    searcherManager.maybeReopen();
  }

  @Override
  public Map<OsmObject, Float> search(Query query) throws IOException {

    IndexSearcher indexSearcher = searcherManager.acquire();
    try {

      final Map<OsmObject, Float> searchResults = new HashMap<OsmObject, Float>();

      Collector collector = new Collector() {
        private Scorer scorer;

        @Override
        public void setScorer(Scorer scorer) throws IOException {
          this.scorer = scorer;
        }

        @Override
        public void collect(int doc) throws IOException {
          Document document = indexReader.document(doc);
          String objectClass = document.get("class");
          OsmObject object;
          if ("node".equals(objectClass)) {
            object = getNode(Long.valueOf(document.get("node.identity")));
          } else if ("way".equals(objectClass)) {
            object = getWay(Long.valueOf(document.get("way.identity")));
          } else if ("relation".equals(objectClass)) {
            object = getRelation(Long.valueOf(document.get("relation.identity")));
          } else {
            throw new RuntimeException("Unknown class " + objectClass);
          }
          searchResults.put(object, scorer.score());
        }

        private IndexReader indexReader;
        private int docBase;

        @Override
        public void setNextReader(IndexReader reader, int docBase) throws IOException {
          this.indexReader = reader;
          this.docBase = docBase;
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
          return true;
        }
      };

      indexSearcher.search(query, collector);

      return searchResults;

    } finally {
      searcherManager.release(indexSearcher);
    }

  }
}
