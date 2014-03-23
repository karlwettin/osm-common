package se.kodapan.osm.domain.root.indexed;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
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

/**
 * Implementation using Lucene 4.5.0.
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

  private FieldType identityField;

  private BytesRef class_nodeByteRef = new BytesRef("node");
  private BytesRef class_wayByteRef = new BytesRef("way");
  private BytesRef class_relationByteRef = new BytesRef("relation");

  private FieldType classField;
  private FieldType classStoreField;
  private FieldType identityStoreField;
  private FieldType coordinateDoubleField;
  private FieldType tagField;

  public IndexedRootImpl(Root decorated) {
    this(decorated, null);
  }

  public IndexedRootImpl(Root decorated, File fileSystemDirectory) {
    super(decorated);
    this.fileSystemDirectory = fileSystemDirectory;

    identityField = new FieldType();
    identityField.setIndexed(true);
    identityField.freeze();

    identityStoreField = new FieldType();
    identityStoreField.setIndexed(false);
    identityStoreField.setDocValueType(FieldInfo.DocValuesType.NUMERIC);
    identityStoreField.freeze();

    classStoreField = new FieldType();
    classStoreField.setIndexed(false);
    classStoreField.setDocValueType(FieldInfo.DocValuesType.BINARY);
    classStoreField.freeze();

    classField = new FieldType();
    classField.setIndexed(true);
    classField.freeze();

    coordinateDoubleField = new FieldType();
    coordinateDoubleField.setIndexed(true);
    coordinateDoubleField.setNumericType(FieldType.NumericType.DOUBLE);
    coordinateDoubleField.setNumericPrecisionStep(4);

    tagField = new FieldType();
    tagField.setIndexed(true);
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
            if (object.isLoaded()) {
              object.accept(indexVisitor);
            } else {
              log.warn("Ignoring non loaded " + object.getClass().getSimpleName() + "#id=" + object.getId());
            }
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

  public void open() throws IOException {
    if (fileSystemDirectory == null) {
      directory = new RAMDirectory();
    } else {
      directory = FSDirectory.open(fileSystemDirectory);
    }
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    indexWriter = new IndexWriter(directory, config);
    searcherManager = new SearcherManager(indexWriter, true, new SearcherFactory());
    open = true;
  }

  public void close() throws IOException {
    searcherManager.close();
    indexWriter.close();
    directory.close();
    open = false;
  }

  private IndexVisitor indexVisitor = new IndexVisitor();

  private class IndexVisitor implements OsmObjectVisitor<Void> {

    private static final long serialVersionUID = 1l;

    private void addObjectFields(OsmObject object, Document document) {
      if (object.getTags() != null) {
        for (Map.Entry<String, String> tag : object.getTags().entrySet()) {
          document.add(new Field("tag.key", tag.getKey(), tagField));
          document.add(new Field("tag.value", tag.getValue(), tagField));
          document.add(new Field("tag.key_and_value", tag.getKey() + "=" + tag.getValue(), tagField));
        }
      }
    }

    @Override
    public Void visit(Node node) {
      Document document = new Document();
      document.add(new Field("class", "node", classField));
      document.add(new BinaryDocValuesField("class_value", class_nodeByteRef));
      document.add(new Field("node.identity", String.valueOf(node.getId()), classField));
      document.add(new NumericDocValuesField("node.identity_value", node.getId()));

      if (node.isLoaded()) {
        document.add(new DoubleField("node.latitude", node.getLatitude(), coordinateDoubleField));
        document.add(new DoubleField("node.longitude", node.getLongitude(), coordinateDoubleField));
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
      document.add(new Field("class", "way", classField));
      document.add(new BinaryDocValuesField("class_value", class_wayByteRef));
      document.add(new Field("way.identity", String.valueOf(way.getId()), classField));
      document.add(new NumericDocValuesField("way.identity_value", way.getId()));

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
          document.add(new DoubleField("way.envelope.south_latitude", southLatitude, coordinateDoubleField));
          document.add(new DoubleField("way.envelope.west_longitude", westLongitude, coordinateDoubleField));
          document.add(new DoubleField("way.envelope.north_latitude", northLatitude, coordinateDoubleField));
          document.add(new DoubleField("way.envelope.east_longitude", eastLongitude, coordinateDoubleField));
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
      document.add(new Field("class", "relation", classField));
      document.add(new BinaryDocValuesField("class_value", class_relationByteRef));
      document.add(new Field("relation.identity", String.valueOf(relation.getId()), classField));
      document.add(new NumericDocValuesField("relation.identity_value", relation.getId()));

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

  public void commit() throws IOException {
    indexWriter.commit();
    searcherManager.maybeRefresh();
  }


  public Map<OsmObject, Float> search(Query query) throws IOException {

    IndexSearcher indexSearcher = searcherManager.acquire();
    try {

      final Map<OsmObject, Float> searchResults = new HashMap<OsmObject, Float>();

      Collector collector = new Collector() {
        private Scorer scorer;
        private AtomicReaderContext context;

        @Override
        public void setScorer(Scorer scorer) throws IOException {
          this.scorer = scorer;
        }


        @Override
        public void collect(int doc) throws IOException {
          OsmObject object;
          BytesRef bytesRef = new BytesRef(10);
          context.reader().getBinaryDocValues("class_value").get(doc, bytesRef);
          if (class_nodeByteRef.bytesEquals(bytesRef)) {
            object = getNode(context.reader().getNumericDocValues("node.identity_value").get(doc));
          } else if (class_wayByteRef.bytesEquals(bytesRef)) {
            object = getWay(context.reader().getNumericDocValues("way.identity_value").get(doc));
          } else if (class_relationByteRef.bytesEquals(bytesRef)) {
            object = getRelation(context.reader().getNumericDocValues("relation.identity_value").get(doc));
          } else {
            throw new RuntimeException();
          }

          searchResults.put(object, scorer.score());
        }

        @Override
        public void setNextReader(AtomicReaderContext context) throws IOException {
          this.context = context;
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
