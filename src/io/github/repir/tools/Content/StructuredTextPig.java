package io.github.repir.tools.Content;

import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchSection;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Strcutures data in a tab-delimited file for processing with Pig.
 * <p/>
 * @author jeroen
 */
public abstract class StructuredTextPig extends StructuredTextCSV {

   public static Log log = new Log(StructuredTextPig.class);
   private static final ByteSearch open = ByteSearch.create("");
   private static final ByteSearch close = ByteSearch.create("\t");
   private static final ByteSearch empty = ByteSearch.create("");
   private static final ByteSearch braceopen = ByteSearch.create("\\{");
   private static final ByteSearch braceclose = ByteSearch.create("\\}\\s?");
   private static final ByteSearch brackopen = ByteSearch.create("\\,?\\s?\\(");
   private static final ByteSearch brackclose = ByteSearch.create("\\)");
   private static final ByteSection brace = braceopen.toSection(braceclose);
   private static final ByteSection brack = brackopen.toSection(brackclose);
   private static final ByteSearch closeline = ByteSearch.create("($|\n)");

   public StructuredTextPig(BufferReaderWriter reader) {
      super(reader);
      root = this.addRoot("root", "", "($|\n)", "", "\n");
   }

   public StructuredTextPig(Datafile writer) {
      super(writer);
      root = this.addRoot("root", "", "($|\n)", "", "\n");
   }

   private void setTerminators(FolderNode root) {
      Node last = root.orderedfields.get(root.orderedfields.size() - 1);
      for (Node n : root.orderedfields) {
         if (n instanceof FolderNode) {
            setTerminators((FolderNode) n);
            if (n != last)
               n.closelabel = "}\t";
         } else if (n != last && n.close != close) {
            n.close = close;
            n.closelabel = (root == this.root)?"\t":",";
         }
      }
   }

   public class Bag extends FolderNode {

      protected Bag(FolderNode parent, String label) {
         super(parent, label, braceopen, braceclose, "{", "}");
      }

      @Override
      protected void readNode(ByteSearchSection section) {
         for (ByteSearchSection s : section.findAllSectionsDontMove(brack)) {
            this.addAnother();
            for (Node f : orderedfields) {
               ByteSearchSection pos = findSection(s, f.section);
               if (pos.found()) {
                  f.readNode(pos);
                  s.movePast(pos);
               }
            }
         }
      }
      
      @Override
      protected void write(ArrayList list) {
         if (list != null && list.size() > 0) {
            if (openlabel.length() > 0) {
               datafile.printf("%s", openlabel);
            }
            boolean firsttuple = true;
            for (NodeValue v : (ArrayList<NodeValue>) list) {
               if (v.size() > 0) {
                  if (firsttuple) {
                        firsttuple = false;
                     } else {
                        datafile.printf(",");
                     }
                  datafile.printf("(");
                  boolean firstfield = true;
                  for (Node f : orderedfields) {
                     if (firstfield) {
                        firstfield = false;
                     } else {
                        datafile.printf(",");
                     }
                     ArrayList subvalues = v.get(f.label);
                     if (subvalues != null) {
                        f.write(subvalues);
                     } else {
                        log.fatal("Attempted to write an OrderedNode with value %s unset", f.label);
                     }
                  }
                  datafile.printf(")");
               }
            }
            if (closelabel.length() > 0) {
               datafile.printf("%s", closelabel);
            }
         }
      }
   }

   public IntField addInt(FolderNode parent, String label) {
      IntField f = addInt(parent, label, open, empty, "", "");
      setTerminators(root);
      return f;
   }

   public BoolField addBoolean(FolderNode parent, String label) {
      BoolField f = addBoolean(parent, label, open, empty, "", "");
      setTerminators(root);
      return f;
   }

   public LongField addLong(FolderNode parent, String label) {
      LongField f = addLong(parent, label, open, empty, "", "");
      setTerminators(root);
      return f;
   }

   public DoubleField addDouble(FolderNode parent, String label) {
      DoubleField f = addDouble(parent, label, open, empty, "", "");
      setTerminators(root);
      return f;
   }

   public StringField addString(FolderNode parent, String label) {
      StringField f = addString(parent, label, open, empty, "", "");
      setTerminators(root);
      return f;
   }

   public IntField addInt(String label) {
      return addInt(root, label);
   }

   public LongField addLong(String label) {
      return addLong(root, label);
   }

   public DoubleField addDouble(String label) {
      return addDouble(root, label);
   }

   public StringField addString(String label) {
      return addString(root, label);
   }

   public BoolField addBoolean(String label) {
      return addBoolean(root, label);
   }

   public Bag addBag(String label) {
      return addBag(root, label);
   }

   public Bag addBag(FolderNode folder, String label) {
      Bag bag = new Bag(folder, label);
      return bag;
   }

   public void write(StructuredTextPigTuple t) {
      t.write(this);
   }
}
