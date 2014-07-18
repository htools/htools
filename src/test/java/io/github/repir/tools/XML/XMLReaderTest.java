/*
 * Copyright 2014 jeroen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.repir.tools.XML;

import io.github.repir.tools.Structure.XMLReader;
import io.github.repir.tools.Lib.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jeroen
 */
public class XMLReaderTest {
    public static Log log = new Log(XMLReaderTest.class);
    InputStream xmlin;
    XMLReader xmlreader;
    
    @Before
    public void setUp() throws FileNotFoundException, XMLStreamException {
//        xmlin = new FileInputStream("/Users/jeroen/Downloads/queryhistory.xml");
//        xmlreader = new XMLReader(xmlin, "hit");
    }

    @Test
    public void testHasNext() {
//        while (xmlreader.hasNext()) {
//            HashMap<String, Object> next = xmlreader.next();
//            log.printf("hit %s", next);
//        }
    }
}
