package io.github.repir.tools.io.struct;

import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Processes text data, with the elements in a fixed ordered sequence. In contrast
 * to {@link StructuredTextFile}, the elements are not necessarily identified by
 * their context, but by their position. Therefore, all elements must receive a value.
 * This can be used to read and write records with a simple separator such as a 
 * comma or a space, and records separated by an end of line. 
 * <p/>
 * @author jeroen
 */
public abstract class StructuredTextCSV extends StructuredTextFile {

   public static Log log = new Log(StructuredTextCSV.class);
   String open;
   String close;
   ByteSearch regex_open;
   ByteSearch regex_close;

   public StructuredTextCSV(BufferReaderWriter reader) {
      this( reader, "", "\t|$", "", "\t");
   }

   public StructuredTextCSV(BufferReaderWriter reader, String regexopen, String regexclose, String open, String close) {
      super( reader );
      this.open = open;
      this.close = close;
      regex_open = ByteSearch.create(regexopen);
      regex_close = ByteSearch.create(regexclose);
   }

   public StructuredTextCSV(Datafile writer) {
      this( writer, "", "\t|$", "", "\t");
   }

   public StructuredTextCSV(Datafile datafile, String regexopen, String regexclose, String open, String close) {
      super( datafile );
      this.open = open;
      this.close = close;
      regex_open = ByteSearch.create(regexopen);
      regex_close = ByteSearch.create(regexclose);
   }
   
   @Override
   public void rebuildBeforeFirstUse() {
       Node lastNode = getRoot();
       while (lastNode instanceof FolderNode) {
           FolderNode foldernode = (FolderNode)lastNode;
           lastNode = foldernode.orderedfields.get(foldernode.orderedfields.size()-1);
       }
       lastNode.closelabel = "";
       lastNode.setOpenClose(ByteSearch.create(""), ByteSearch.create("$"));
   }

   protected ByteSearchSection findSection(ByteSearchSection section, ByteSection needle) {
      return section.findSectionDontMove(needle);
   }

   protected class OrderedNode extends FolderNode {

      protected OrderedNode(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
         super(parent, label, open, close, openlabel, closelabel);
      }

      @Override
      protected void readNode(ByteSearchSection section) {
         if (nodevalues == null)
            nodevalues = new ArrayList<NodeValue>();
         nodevalue = new NodeValue();
         for (Node f : orderedfields) {
            ByteSearchSection pos = findSection(section, f.section);
            //log.info("%s %b %s %s", f.label, pos.found(), f.section.toString(), pos.reportString());
            if (pos.found()) {
               f.readNode(pos);
               section.movePast(pos);
            }
         }
         if (nodevalue.size() > 0)
            nodevalues.add(nodevalue);
      }
      
      @Override
      protected void write(ArrayList list) {
         if (list != null) {
            for (NodeValue v : (ArrayList<NodeValue>) list) {
               if (openlabel.length() > 0) {
                  datafile.printf("%s", openlabel);
               }
               for (Node f : orderedfields) {
                  ArrayList subvalues = v.get(f.label);
                  if (subvalues != null) {
                     f.write(subvalues);
                  } else {
                     log.fatal("Attempted to write an OrderedNode with value %s unset", f.label);
                  }
               }
               if (closelabel.length() > 0) {
                  datafile.printf("%s", closelabel);
               }
            }
         }
      }
   }

   @Override
   public FolderNode addNode(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
      return new OrderedNode(parent, label, open, close, openlabel, closelabel);
   }

   @Override
   public FolderNode createRoot() {
      return addNode(null, "root" , ByteSearch.create(""), ByteSearch.create("($|\n)"), "", "\n");
   }

   public StringField addString(String label) {
      return addString(getRoot(), label, regex_open, regex_close, open, close);
   }

   public StringArrayField addStringArray(String label) {
      return addStringArray(getRoot(), label, regex_open, regex_close, open, close);
   }

   public IntArrayField addIntArray(String label) {
      return addIntArray(getRoot(), label, regex_open, regex_close, open, close);
   }

   public LongArrayField addLongArray(String label) {
      return addLongArray(getRoot(), label, regex_open, regex_close, open, close);
   }

   public DoubleArrayField addDoubleArray(String label) {
      return addDoubleArray(getRoot(), label, regex_open, regex_close, open, close);
   }

   public JsonField addJson(String label) {
      return addJson(getRoot(), label, regex_open, regex_close, open, close);
   }

   public JsonArrayField addJsonArray(String label, Type clazz) {
      return addJsonArray(getRoot(), label, clazz, regex_open, regex_close, open, close);
   }

   public BoolField addBoolean(String label) {
      return addBoolean(getRoot(), label, regex_open, regex_close, open, close);
   }

   public DoubleField addDouble(String label) {
      return addDouble(getRoot(), label, regex_open, regex_close, open, close);
   }

   public IntField addInt(String label) {
      return addInt(getRoot(), label, regex_open, regex_close, open, close);
   }

   public LongField addLong(String label) {
      return addLong(getRoot(), label, regex_open, regex_close, open, close);
   }
}
