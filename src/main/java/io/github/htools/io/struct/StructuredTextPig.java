package io.github.htools.io.struct;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;
import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Structures data in a tab-delimited file for processing with Pig.
 * <p>
 * @author jeroen
 */
public abstract class StructuredTextPig extends StructuredTextCSV {

    public static Log log = new Log(StructuredTextPig.class);
    private static final ByteSearch open = ByteSearch.create("");
    private static final ByteSearch close = ByteSearch.create("\t");
    private static final ByteSearch closecomma = ByteSearch.create(",\\s*");
    private static final ByteSearch empty = ByteSearch.create("");
    private static final ByteSearch braceopen = ByteSearch.create("\\{");
    private static final ByteSearch braceclose = ByteSearch.create("\\}\\s*");
    private static final ByteSearch brackopen = ByteSearch.create("\\,?\\s*\\(");
    private static final ByteSearch brackclose = ByteSearch.create("\\)");
    private static final ByteSection brace = braceopen.toSection(braceclose);
    private static final ByteSection brack = brackopen.toSection(brackclose);
    private static final ByteSearch closeline = ByteSearch.create("($|\n)");

    public StructuredTextPig(BufferReaderWriter reader) {
        super(reader);
    }

    public StructuredTextPig(Datafile writer) {
        super(writer);
    }

    @Override
    public FolderNode createRoot() {
        return addNode(null, "root", ByteSearch.create(""), ByteSearch.create("($|\n)"), "", "\n");
    }

    /**
     * Before first use, the last node in each (nested) folder is modified and
     * the separators within bags are changed to , instead of \t
     */
    @Override
    public void rebuildBeforeFirstUse() {
        setTerminators(getRoot());
    }

    private void setTerminators(FolderNode root) {
        Node last = root.orderedfields.get(root.orderedfields.size() - 1);
        for (Node n : root.orderedfields) {
            if (n instanceof FolderNode) {
                setTerminators((FolderNode) n);
                if (n != last) {
                    n.closelabel = "}\t";
                }
            } else if (n != last) {
                if (root == getRoot()) {
                    n.setOpenClose(open, close);
                    n.closelabel = "\t";
                } else {
                    n.setOpenClose(open, closecomma);
                    n.closelabel = ",";
                }
            }
        }
    }

    public class Bag extends FolderNode {

        protected Bag(FolderNode parent, String label) {
            super(parent, label, braceopen, braceclose, "{", "}");
        }

        @Override
        protected ByteSearchSection readNode(ByteSearchSection section) {
            for (ByteSearchSection s : section.findAllSections(brack)) {
                this.addAnother();
                for (Node f : orderedfields) {
                    ByteSearchSection pos = findSection(s, f.section);
                    if (pos.found()) {
                        pos = f.readNode(pos);
                        s.movePast(pos);
                    }
                }
            }
            return section;
        }

        @Override
        protected void write(ArrayList list) throws IOException {
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
        return addInt(parent, label, open, empty, "", "");
    }

    public BoolField addBoolean(FolderNode parent, String label) {
        return addBoolean(parent, label, open, empty, "", "");
    }

    public LongField addLong(FolderNode parent, String label) {
        return addLong(parent, label, open, empty, "", "");
    }

    public DoubleField addDouble(FolderNode parent, String label) {
        return addDouble(parent, label, open, empty, "", "");
    }

    public StringField addString(FolderNode parent, String label) {
        return addString(parent, label, open, empty, "", "");
    }

    public IntField addInt(String label) {
        return addInt(getRoot(), label);
    }

    public LongField addLong(String label) {
        return addLong(getRoot(), label);
    }

    public DoubleField addDouble(String label) {
        return addDouble(getRoot(), label);
    }

    public StringField addString(String label) {
        return addString(getRoot(), label);
    }

    public BoolField addBoolean(String label) {
        return addBoolean(getRoot(), label);
    }

    public Bag addBag(String label) {
        return addBag(getRoot(), label);
    }

    public Bag addBag(FolderNode folder, String label) {
        Bag bag = new Bag(folder, label);
        return bag;
    }

    public void write(StructuredTextPigTuple t) {
        t.write(this);
    }
}
