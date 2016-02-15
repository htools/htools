package io.github.htools.io.struct;

import io.github.htools.io.Datafile;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import io.github.htools.search.*;

import java.util.ArrayList;

/**
 * Extends {@link StructuredTextFile} to read XML files. 
 * <p>
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
      return section.findAllSections(needle);
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
      public ByteSearchSection readNode(ByteSearchSection section) {
          
         // process attributes in tag
         ArrayList<ByteSearchPosition> positions = attribute.findAllPos(section.haystack, section.start, section.innerstart);
         for (ByteSearchPosition pos : positions) {
            int posstart = pos.start+1; // eat space
            int posend = pos.end - 1;
            String attr = attributename.findAsFullTrimmedString(section.haystack, posstart, posend);
            Node datanode = nestedfields.get(attr);
            if (datanode != null) {
               posstart = valuestart.findEnd(section.haystack, posstart, posend);
               ByteSearchSection subsection = new ByteSearchSection(section.haystack, posstart, posstart, posend, posend);
               datanode.readNode(subsection);
            }
         }
         //Node ff = orderedfields.get(2);
         //ByteSearchSection pos1 = section.findSection(ff.section);
         //log.info("scan %s regex %s found %s", ff.label, ff.section, pos1);
         if (isOpenClose(section)) {
             return new ByteSearchSection(section.haystack, section.start, section.innerstart, section.innerstart, section.innerstart);
         } else {
            // process attributes between open and close tag 
            for (Node f : orderedfields) {
               for (ByteSearchSection pos : findAllSections(section, f.section)) {
                  f.addAnother();
                  f.readNode(pos);
               }
            }
         }
         return section;
      }

      private boolean isOpenClose(ByteSearchSection section) {
         for (int p = section.innerstart - 2; p > section.start; p--) {
            if (section.haystack[p] == '/') {
               return true;
            }
            if (!ByteTools.isWhiteSpace(section.haystack[p])) {
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
   
   public LongField addLong(FolderNode parent, String label) {
      return new LongField(parent, label, xmlOpenData(label), xmlCloseData(label), xmlOpen(label), xmlClose(label));
   }
   
   public StringField addString(FolderNode parent, String label) {
      return new StringField(parent, label, xmlOpenData(label), xmlCloseData(label), xmlOpen(label), xmlClose(label));
   }
   
   public DoubleField addDouble(FolderNode parent, String label) {
      return new DoubleField(parent, label, xmlOpenData(label), xmlCloseData(label), xmlOpen(label), xmlClose(label));
   }
}
