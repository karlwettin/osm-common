osm-common
==========

Library for accessing OpenStreetMap services, parsing and processing data.

Best documentation is found in test cases.

API mainly include support for Nominatim, Overpass, changeset stores and osm.xml-parsing.

Module core contains several abstract classes that are implemented in module java and module android.
Main difference between java and android modules is what XML-stream API and what version of Lucene that is used.

```
se.kodapan.osm.domain:OsmObject
  #getId():long
  #setTag(key, value)
  #getTag(key)

se.kodapan.osm.domain:Node extends OsmObject
se.kodapan.osm.domain:Way extends OsmObject
se.kodapan.osm.domain:Relation extends OsmObject

/** Abstract store for keeping track of OSM objects */
se.kodapan.osm.domain.root:Root
  #getNode(id):Node
  #getWay(id):Way
  #getRelation(id):Relation
  #add(osmObject)

/** RAM-store for keeping track of OSM objects */
se.kodapan.osm.domain.root:PojoRoot

/** Inverted index decorating any Root, allowing for fast advanced local queries alá Overpass */
se.kodapan.osm.domain.root.indexed:IndexedRoot<Query> extends Root
  #search(Query):Map<OsmObject, Float>
  #commit()
  #reconstruct()

/** Create, update and delete OSM objects in a Root based on the content of an .osm.xml  */
se.kodapan.osm.parser.xml.instantiated:InstantiatedOsmXmlParser
 #setRoot(root)
 #read(xml)

/** Streaming API for processing those TB-sized osm.xml-files */
se.kodapan.osm.parser.xml.streaming:StreamingOsmXmlParser
  #read(xml)
  #processParsedNode(node)
  #processParsedWay(way)
  #processParsedRelation(relation)


se.kodapan.osm.services.changesetstore:ChangesetStore
  #setBaseUrl()
  #findFirstChangesetStateSince(timestamp)
  #findChangesetStatesSince(timestamp)

se.kodapan.osm.services.overpass:Overpass
se.kodapan.osm.services.overpass:FileSystemCachedOverpass
  #execute(query):String

se.kodapan.osm.services.nominatim:Nominatim
se.kodapan.osm.services.nominatim:FileSystemCachedNominatim
  #search(url):String

se.kodapan.osm.services.nominatim:NominatimJsonResponseParser
  #setRoot(root)
  #parse(nominatimJsonResponse):List<NominatimJsonResponseParser.Result>

se.kodapan.osm.services.nominatim:NominatimJsonResponseParser.Result
  #getImportance():double
  #getObject:OsmObject


se.kodapan.osm.services.nominatim:NominatimQueryBuilder
  #build():String
  #setBaseURL(url)
  #setFormat(format)
  #setQuery(query)
  #addCountryCode(iso)
  #setLimit(limit)
  ...
```


There is also a bit of tools surrounding this API such as filters to remove matching OsmObjects from a collection,
distance metrics and such.
