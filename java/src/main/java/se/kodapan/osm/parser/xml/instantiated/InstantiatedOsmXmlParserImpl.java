package se.kodapan.osm.parser.xml.instantiated;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;

/**
 * An .osm.xml and .osc.xml parser
 * into a fully instantiated object graph.
 * <p/>
 * This class is not thread safe!
 *
 * @author kalle
 * @since 2013-03-27 21:41
 */
public class InstantiatedOsmXmlParserImpl extends AbstractStreamingInstantiatedOsmXmlParser {


  private static final Logger log = LoggerFactory.getLogger(InstantiatedOsmXmlParserImpl.class);


  private XMLInputFactory xmlif = XMLInputFactory.newInstance();

  @Override
  public Stream readerFactory(final Reader xml) throws StreamException {

    final XMLStreamReader xmlr;
    try {
      xmlr = xmlif.createXMLStreamReader(xml);
    } catch (javax.xml.stream.XMLStreamException e) {
      throw new StreamException(e);
    }

    return new Stream() {


      @Override
      public int getEventType() throws StreamException {
        return xmlr.getEventType();
      }

      @Override
      public boolean hasNext() throws StreamException {
        try {
          return xmlr.hasNext();
        } catch (XMLStreamException e) {
          throw new StreamException(e);
        }

      }

      @Override
      public int next() throws StreamException {
        try {
          return xmlr.next();
        } catch (XMLStreamException e) {
          throw new StreamException(e);
        }

      }

      @Override
      public boolean isStartElement(int eventType) throws StreamException {
        return eventType == XMLStreamConstants.START_ELEMENT;
      }

      @Override
      public boolean isEndElement(int eventType) throws StreamException {
        return eventType == XMLStreamConstants.END_ELEMENT;
      }

      @Override
      public String getLocalName() throws StreamException {
        return xmlr.getLocalName();
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
        return xmlr.getAttributeLocalName(index);
      }

      @Override
      public void close() throws StreamException {
        try {
          xmlr.close();
        } catch (XMLStreamException e) {
          throw new StreamException(e);
        }
      }
    };
  }

}

