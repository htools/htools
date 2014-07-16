package io.github.repir.tools.Structure;

import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSearchSection;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Provides streamed nodevalue access by iterative read/write actions of
 * records/rows that consists of some defined structure. Upon initialization,
 * the implementing class should override {@link #initStructure()}, in which the
 * structure is defined adding fields that extend {@link Element} to the
 * structure. These fields must be used to read/write rows in the exact order
 * the fields were added to the structure. Structure protects nodevalue
 * integrity by checking if reading/writing is performed according to the
 * structure definition, and throws a fatal error otherwise.
 * <p/>
 * @author jeroen
 */
public abstract class StructuredTextFile {

    public static Log log = new Log(StructuredTextFile.class);
    private final ByteSearch NoReader = null;
    public Datafile datafile;  // the output of the Stream
    public BufferReaderWriter reader;  // the input of the Stream
    private final FolderNode root;
    private boolean firstUse = true;

    public StructuredTextFile(BufferReaderWriter readerwriter) {
        this.reader = readerwriter;
        root = createRoot();
    }

    public StructuredTextFile(Datafile writer) {
        this(writer.rwbuffer);
        this.datafile = writer;
    }
    
    public Datafile getDatafile() {
        return datafile;
    }

    /**
     * StructuredTextFile needs a root FolderNode that symbolizes a record. 
     * Implementations of StructuredTextFile supply this by overriding this
     * method. The root node should be the only one that has no parent. 
     * @return 
     */
    public abstract FolderNode createRoot();

    public void openRead() {
        checkFirstUse();
        if (datafile != null) {
            datafile.openRead();
        }
    }

    public void openWrite() {
        checkFirstUse();
        if (root.writeenabled() && datafile != null) {
            datafile.openWrite();
        }
    }

    public void closeRead() {
        if (datafile != null) {
            datafile.closeRead();
        }
    }

    public void closeWrite() {
        if (datafile != null) {
            datafile.closeWrite();
        }
    }
    
    public void checkFirstUse() {
        if (firstUse) {
            rebuildBeforeFirstUse();
            firstUse=false;
        }
    }

    /**
     * Override this to change the structure before the first use, e.g. for
     * TSV, normal fields are terminated with a \t but after the last field
     * this may be omitted.
    */
    public void rebuildBeforeFirstUse() {}
    
    public boolean exists() {
        return datafile.exists();
    }

    public boolean lock() {
        return datafile.lock();
    }

    public void unlock() {
        datafile.unlock();
    }

    public FolderNode getRoot() {
        return root;
    }

    public void openAppend() {
        if (!datafile.hasLock()) {
            throw new RuntimeException(PrintTools.sprintf("Should lock file before append %s", datafile.getFilename()));
        }
        datafile.openAppend();
    }

    public boolean next() {
        try {
            while (true) {
                ByteSearchSection section = reader.findSectionStart(root.section);
                if (validRecord(section)) {
                    root.emptyDataContainer();
                    root.readNode(section);
                    reader.movePast(section);
                    return true;
                }
                reader.movePast(section);
                if (!reader.hasMore())
                    break;
            }
        } catch (EOCException ex) {
        }
        return false;
    }

    protected boolean validRecord(ByteSearchSection section) {
        return section.notEmpty();
    }

    public void read() {
        openRead();
        try {
            while (true) {
                ByteSearchSection section = reader.findSectionStart(root.section);
                if (section.notEmpty()) {
                    root.readNode(section);
                    reader.movePast(section);
                }
            }
        } catch (EOCException ex) {
        }
        closeRead();
    }

    public void write() {
        if (root.nodevalues != null) {
            root.write(root.nodevalues);
        }
        root.emptyDataContainer();
    }

    protected ArrayList<ByteSearchSection> findAllSections(ByteSearchSection section, ByteSection needle) {
        return section.findAllSectionsDontMove(needle);
    }

    public class NodeValue extends HashMap<String, ArrayList> {

        public NodeValue get(FolderNode f) {
            return (get(f.label) != null) ? ((NodeValue) get(f.label).get(0)) : null;
        }

        public ArrayList<NodeValue> getListNode(FolderNode f) {
            return (ArrayList<NodeValue>) get(f.label);
        }

        public ArrayList getListData(DataNode f) {
            return get(f.label);
        }

        public Object get(DataNode f) {
            return (get(f.label) != null) ? get(f.label).get(0) : null;
        }
    }

    /**
     * The building brick of a structure definition. Fields of any type
     * extending Node can be added to the structure. Values can be written/read
     * using the fields through their local read/write methods. Each field
     * should have a value variable that contains the last value written/read.
     */
    public abstract class Node {

        public final String label;
        final FolderNode parent;
        protected ByteSection section;
        ByteSearchPosition next;
        String openlabel;
        String closelabel;

        protected Node(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            this.label = label;
            this.parent = parent;
            if (parent != null) {
                parent.addField(this);
            }
            setOpenClose(open, close);
            this.openlabel = openlabel;
            this.closelabel = closelabel;
        }

        public void setOpenClose(ByteSearch open, ByteSearch close) {
            if (open != null && close != null) {
                this.section = new ByteSection(open, close);
            }
        }

        protected boolean writeenabled() {
            if (openlabel == null || closelabel == null) {
                return false;
            }
            return true;
        }

        protected abstract void emptyDataContainer();

        protected abstract void write(ArrayList values);

        protected abstract void readNode(ByteSearchSection section);

        protected abstract void addAnother();
    }

    /**
     * The building brick of a structure definition. Fields of any type
     * extending Node can be added to the structure. Values can be written/read
     * using the fields through their local read/write methods. Each field
     * should have a value variable that contains the last value written/read.
     */
    public abstract class DataNode<T> extends Node {

        ByteSearch match;
        ArrayList<T> value;

        protected DataNode(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
            this.match = match();
        }

        protected ByteSearch match() {
            return ByteSearch.create(".*");
        }

        protected String stringValue(ByteSearchSection section) {
            return section.toFullTrimmedString();
        }

        protected abstract T value(ByteSearchSection outersection);

        protected abstract String toString(T value);

        public void set(T t) {
            if (value == null) {
                value = new ArrayList<T>();
                parent.putValue(this, value);
            }
            value.add(t);
        }

        @Override
        protected void readNode(ByteSearchSection outersection) {
            T value = value(outersection);
            //log.info("%s %s", label, value);
            set(value);
        }

        @Override
        protected void write(ArrayList list) {
            for (T t : (ArrayList<T>) list) {
                datafile.printf("%s%s%s", openlabel, toString(t), closelabel);
            }
        }

        public T get() {
            ArrayList<T> list = parent.get(this);
            return (list != null && list.size() > 0) ? list.get(0) : null;
        }

        public T get(NodeValue parentvalue) {
            Object v = parentvalue.get(this);
            return (v != null) ? (T) v : null;
        }

        public ArrayList<T> getList(NodeValue parentvalue) {
            return parentvalue.getListData(this);
        }

        public ArrayList<T> getList() {
            return parent.get(this);
        }

        protected void emptyDataContainer() {
            value = null;
        }

        protected void addAnother() {
        }
    }

    public class FolderNode extends Node implements Iterable<NodeValue> {

        public HashMap<String, Node> nestedfields = new HashMap<String, Node>();
        public ArrayList<Node> orderedfields = new ArrayList<Node>();
        protected NodeValue nodevalue;
        protected ArrayList<NodeValue> nodevalues;

        protected FolderNode(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        protected void addField(Node f) {
            if (nestedfields.containsKey(f.label))
                log.fatal("cannot use the same label %s in the same FolderNode %s twice", f.label, this.label);
            nestedfields.put(f.label, f);
            orderedfields.add(f);
        }

        protected void putValue(Node f, ArrayList value) {
            if (nodevalue == null) {
                if (nodevalues == null) {
                    nodevalues = new ArrayList<NodeValue>();
                    if (parent != null) // for root
                    {
                        parent.putValue(this, nodevalues);
                    }
                }
                nodevalue = new NodeValue();
                nodevalues.add(nodevalue);
            }
            nodevalue.put(f.label, value);
        }

        public ArrayList<NodeValue> get(FolderNode f) {
            if (nodevalues != null && nodevalues.size() > 0) {
                return nodevalues.get(0).get(f.label);
            }
            return null;
        }

        public ArrayList get(DataNode f) {
            if (nodevalues != null && nodevalues.size() > 0) {
                return nodevalues.get(0).get(f.label);
            }
            return null;
        }

        public ArrayList<NodeValue> get() {
            return nodevalues;
        }

        @Override
        protected void readNode(ByteSearchSection section) {
            for (Node f : nestedfields.values()) {
                for (ByteSearchSection pos : findAllSections(section, f.section)) {
                    f.addAnother();
                    f.readNode(pos);
                }
            }
        }

        @Override
        public void addAnother() {
            if (nodevalue != null && nodevalue.size() > 0) {
                for (String label : nodevalue.keySet()) {
                    nestedfields.get(label).emptyDataContainer();
                }
                nodevalue = null;
            }
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
                        }
                    }
                    if (closelabel.length() > 0) {
                        datafile.printf("%s", closelabel);
                    }
                }
            }
        }

        @Override
        protected boolean writeenabled() {
            boolean writeenabled = super.writeenabled();
            for (Node n : orderedfields) {
                writeenabled |= n.writeenabled();
            }
            return writeenabled;
        }

        @Override
        protected void emptyDataContainer() {
            nodevalues = null;
            nodevalue = null;
            for (Node f : orderedfields) {
                f.emptyDataContainer();
            }
        }

        public int size() {
            return (nodevalues != null) ? nodevalues.size() : 0;
        }

        public Iterator<NodeValue> iterator() {
            return (nodevalues != null) ? nodevalues.iterator() : null;
        }
    }

    public class DoubleField extends DataNode<Double> {

        protected DoubleField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public Double value(ByteSearchSection section) {
            return Double.parseDouble(stringValue(section));
        }

        public String toString(Double value) {
            return value.toString();
        }
    }

    public class IntField extends DataNode<Integer> {

        protected IntField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public Integer value(ByteSearchSection section) {
            return Integer.parseInt(stringValue(section));
        }

        public String toString(Integer value) {
            return value.toString();
        }
    }

    public class LongField extends DataNode<Long> {

        protected LongField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public Long value(ByteSearchSection section) {
            return Long.parseLong(stringValue(section));
        }

        public String toString(Long value) {
            return value.toString();
        }
    }

    public class StringField extends DataNode<String> {

        protected StringField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public String value(ByteSearchSection section) {
            return this.stringValue(section);
        }

        @Override
        public String toString(String value) {
            return value;
        }
    }

    public class BoolField extends DataNode<Boolean> {

        protected BoolField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public Boolean value(ByteSearchSection section) {
            return Boolean.parseBoolean(this.stringValue(section));
        }

        @Override
        public String toString(Boolean value) {
            return value.toString();
        }
    }

    public IntField addInt(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new IntField(parent, label, open, close, openlabel, closelabel);
    }

    public IntField addInt(String label, String open, String close, String openlabel, String closelabel) {
        return addInt(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public IntField addInt(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addInt(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }
    
    public BoolField addBoolean(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new BoolField(parent, label, open, close, openlabel, closelabel);
    }

    public BoolField addBoolean(String label, String open, String close, String openlabel, String closelabel) {
        return addBoolean(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public BoolField addBoolean(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addBoolean(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }
    
    public LongField addLong(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new LongField(parent, label, open, close, openlabel, closelabel);
    }

    public LongField addLong(String label, String open, String close, String openlabel, String closelabel) {
        return addLong(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public LongField addLong(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addLong(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }
    
    public DoubleField addDouble(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new DoubleField(parent, label, open, close, openlabel, closelabel);
    }

    public DoubleField addDouble(String label, String open, String close, String openlabel, String closelabel) {
        return addDouble(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public DoubleField addDouble(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addDouble(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public StringField addString(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new StringField(parent, label, open, close, openlabel, closelabel);
    }

    public StringField addString(String label, String open, String close, String openlabel, String closelabel) {
        return addString(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public StringField addString(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addString(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }
    
    public FolderNode addNode(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new FolderNode(parent, label, open, close, openlabel, closelabel);
    }

    public FolderNode addNode(String label, String open, String close, String openlabel, String closelabel) {
        return addNode(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public FolderNode addNode(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addNode(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }
}
