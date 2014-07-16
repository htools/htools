package io.github.repir.tools.Structure;

import io.github.repir.tools.Structure.StructuredTextFile;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSearchSection;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.ByteTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.util.ArrayList;

/**
 * Extends {@link StructuredTextFile} to read XML files. 
 * <p/>
 * @author jeroen
 */
public abstract class StructuredTextXML extends StructuredTextFile {

   public static Log log = new Log(StructuredTextXML.class);

   public StructuredTextXML(BufferReaderWriter reader) {
      super( reader );
   }

   public StructuredTextXML(Datafile readerwriter) {
      super(readerwriter);
   }

   @Override
   protected ArrayList<ByteSearchSection> findAllSections( ByteSearchSection section, ByteSection needle ) {
      return section.findAllSectionsDontMove(needle);
   }
   
   private static ByteSection xmlOpenNode(String label) {
      return new ByteSection(PrintTools.sprintf("<%s(?=[\\s/>])", label), ">").innerQuoteSafe();
   }

   private static ByteSearch xmlCloseNode(String label, FolderNode parent) {
      ByteRegex closeNode = new ByteRegex(PrintTools.sprintf("</%s\\s*>", label));
      if (parent == null)
         return closeNode;
      ByteRegex closeParent = new ByteRegex(PrintTools.sprintf("</%s\\s*>", parent.label));
      ByteRegex closeCombi = ByteRegex.combine(closeNode, closeParent);
      return closeCombi;
   }

   private static ByteSearch xmlOpenData(String label) {
      return ByteSearch.create(PrintTools.sprintf("<%s\\s*>", label));
   }

   private static ByteSearch xmlCloseData(String label) {
      return ByteSearch.create(PrintTools.sprintf("</%s\\s*>", label));
   }

   private static String xmlOpen(String label) {
      return PrintTools.sprintf("<%s>", label);
   }

   private static String xmlClose(String label) {
      return PrintTools.sprintf("</%s>", label);
   }

   private static ByteSearch attribute = ByteSearch.create("\\s[A-Z]\\c*\\s*=\\s*\\Q").QuoteSafe();
   private static ByteRegex attributename = new ByteRegex("[A-Z]\\c*");
   private static ByteRegex valuestart = new ByteRegex("=\\s*[\"']");

   protected class XMLNode extends FolderNode {

      protected XMLNode(FolderNode parent, String label) {
         super(parent, label, xmlOpenNode(label), xmlCloseNode(label, parent), xmlOpen(label), xmlClose(label));
      }
      
      @Override
      public void readNode(ByteSearchSection section) {
         
         // process attributes in tag
         ArrayList<ByteSearchPosition> positions = attribute.findAllPos(section.haystack, section.start, section.innerstart);
         for (ByteSearchPosition pos : positions) {
            pos.start++; // eat space
            pos.end--;
            String attr = attributename.findAsFullTrimmedString(section.haystack, pos.start, pos.end);
            Node datanode = nestedfields.get(attr);
            if (datanode != null) {
               pos.start = valuestart.findEnd(section.haystack, pos.start, pos.end);
               ByteSearchSection subsection = new ByteSearchSection(section.haystack, pos.start, pos.start, pos.end, pos.end);
               datanode.readNode(subsection);
            }
         }
         //Node ff = orderedfields.get(2);
         //ByteSearchSection pos1 = section.findSectionDontMove(ff.section);
         //log.info("scan %s regex %s found %s", ff.label, ff.section, pos1);
         if (isOpenClose(section)) {
            section.innerend = section.innerstart;
            section.end = section.innerstart;
         } else {
            // process attributes between open and close tag 
            for (Node f : orderedfields) {
               for (ByteSearchSection pos : findAllSections(section, f.section)) {
                  f.addAnother();
                  f.readNode(pos);
               }
            }
         }
      }

      private boolean isOpenClose(ByteSearchSection section) {
         for (int p = section.innerstart - 2; p > section.start; p--) {
            if (section.haystack[p] == '/') {
               return true;
            }
            if (!ByteTools.whitespace[section.haystack[p] & 0xFF]) {
               return false;
            }
         }
         return false;
      }
   }

   public FolderNode addNode(FolderNode parent, String label) {
      return new XMLNode(parent, label);
   }
   
   public IntField addInt(FolderNode parent, String label) {
      return new IntField(parent, label, xmlOpenData(label), xmlCloseData(label), xmlOpen(label), xmlClose(label));
   }
   
   public StringField addString(FolderNode parent, String label) {
      return new StringField(parent, label, xmlOpenData(label), xmlCloseData(label), xmlOpen(label), xmlClose(label));
   }
   
   public DoubleField addDouble(FolderNode parent, String label) {
      return new DoubleField(parent, label, xmlOpenData(label), xmlCloseData(label), xmlOpen(label), xmlClose(label));
   }
}
