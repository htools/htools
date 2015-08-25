package io.github.htools.io.struct;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Reads an Record elements from an XML InputStream into a (nested) HashMap.
 * <p>
 * Nodes that have only a character elements are considered to be the same as an
 * attribute, e.g. &lt;item name="name"/&gt; is the same as &lt;item&gt;&lt;name&gt;name&lt;/name&gt;&lt;/item&gt;
 * and results in a HashMap where name=&gt;name is a key=&gt;value pair directly under
 * the item node.
 * @author jeroen
 */
public class XMLReader {

    final String RECORDLABEL;
    final XMLInputFactory inputFactory;
    final XMLEventReader eventReader;

    public XMLReader(InputStream in, String recordlabel) throws XMLStreamException {
        RECORDLABEL = recordlabel;
        inputFactory = XMLInputFactory.newInstance();
        eventReader = inputFactory.createXMLEventReader(in);
    }

    public boolean hasNext() {
        return eventReader.hasNext();
    }

    public HashMap<String, Object> next() {
        while (eventReader.hasNext()) {
            try {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    // If we have an item element, we create a new item
                    if (startElement.getName().getLocalPart() == (RECORDLABEL)) {
                        return readRec(eventReader, startElement);
                    }
                }
            } catch (XMLStreamException ex) {
                Logger.getLogger(XMLReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public HashMap<String, Object> readRec(XMLEventReader reader, StartElement rootElement) throws XMLStreamException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Iterator<Attribute> attributes = rootElement.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            map.put(attribute.getName().toString(), attribute.getValue());
        }
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                HashMap<String, Object> submap = readRec(reader, startElement);
                if (submap.size() == 0) {
                    continue;
                }
                if (submap.size() == 1) {
                    String key = submap.keySet().toArray(new String[1])[0];
                    Object value = submap.get(key);
                    if (key == null && !(value instanceof HashMap)) {
                        store(map, startElement.getName().getLocalPart(), value);
                        continue;
                    }
                }
                store(map, startElement.getName().getLocalPart(), submap);
                continue;
            }
            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if (!endElement.getName().getLocalPart().equals(rootElement.getName().getLocalPart())) {
                    throw new RuntimeException("Malformed input");
                }
                return map;
            }
            if (event.isCharacters()) {
                String value = event.asCharacters().getData().trim();
                if (value.length() > 0) {
                    map.put(null, value);
                }
            }
        }
        return map;
    }
    
    public void store(HashMap<String, Object> map, String key, Object value) {
        Object exists = map.get(key);
        if (exists != null) {
            if (exists instanceof ArrayList) {
                ((ArrayList)exists).add(value);
            } else {
                ArrayList list = new ArrayList();
                list.add(exists);
                list.add(value);
                map.put(key, list);
            }
        } else {
            map.put(key, value);
        }
    }
}
