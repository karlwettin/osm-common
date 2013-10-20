package se.kodapan.osm.parser.xml.instantiated;


import android.util.Xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parses an .osm.xml and .osc.xml
 * into a fully instantiated object graph.
 *
 * @author kalle
 * @since 2013-03-27 21:41
 */
public class InstantiatedOsmXmlParserImpl extends AbstractStreamingInstantiatedOsmXmlParser {

  private static final Logger log = LoggerFactory.getLogger(InstantiatedOsmXmlParserImpl.class);


  @Override
  public Stream readerFactory(InputStream xml) throws StreamException {


    final XmlPullParser xmlr = Xml.newPullParser();
    try {
      xmlr.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      xmlr.setInput(xml, null);
    } catch (XmlPullParserException e) {
      throw new StreamException(e);
    }


    return new Stream() {


      @Override
      public int getEventType() throws StreamException {
        try {
          return xmlr.getEventType();
        } catch (XmlPullParserException e) {
          throw new StreamException(e);
        }
      }


      @Override
      public boolean isEndDocument(int eventType) throws StreamException {
        return eventType == XmlPullParser.END_DOCUMENT;
      }

      @Override
      public int next() throws StreamException {
        try {
          return xmlr.next();
        } catch (XmlPullParserException e) {
          throw new StreamException(e);
        } catch (IOException e) {
          throw new StreamException(e);
        }
      }

      @Override
      public boolean isStartElement(int eventType) throws StreamException {
        return eventType == XmlPullParser.START_TAG;
      }

      @Override
      public boolean isEndElement(int eventType) throws StreamException {
        return eventType == XmlPullParser.END_TAG;
      }

      @Override
      public String getLocalName() throws StreamException {
        return xmlr.getName();
      }

      @Override
      public String getAttributeValue(String what, String key) throws StreamException {
        return xmlr.getAttributeValue(what, key);
      }

      @Override
      public int getAttributeCount() throws StreamException {
        return xmlr.getAttributeCount();
      }

      @Override
      public String getAttributeValue(int index) throws StreamException {
        return xmlr.getAttributeValue(index);
      }

      @Override
      public String getAttributeLocalName(int index) throws StreamException {
        return xmlr.getAttributeName(index);
      }

      @Override
      public void close() throws StreamException {

      }
    };
  }
}

