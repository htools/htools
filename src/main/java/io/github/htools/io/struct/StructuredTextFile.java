package io.github.htools.io.struct;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import io.github.htools.lib.StrTools;
import io.github.htools.type.Long128;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Provides streamed nodevalue access by iterative read/write actions of
 * records/rows that consists of some defined structure.
 * <p>
 * @author jeroen
 */
public abstract class StructuredTextFile {

    public static Log log = new Log(StructuredTextFile.class);
    private final ByteSearch NoReader = null;
    protected Datafile datafile;  // the output of the Stream
    protected BufferReaderWriter reader;  // the input of the Stream
    private final FolderNode root;
    private boolean firstUse = true;
    private boolean hasHeader = false;

    public StructuredTextFile(BufferReaderWriter readerwriter) {
        this.reader = readerwriter;
        root = createRoot();
    }

    public StructuredTextFile(Datafile writer) {
        this.datafile = writer;
        //if (!datafile.rwbuffer.hasRequestedBufferSize())
        //    datafile.setBufferSize(20000);
        root = createRoot();
    }

    public Datafile getDatafile() {
        return datafile;
    }

    protected void setDatafile(Datafile df) {
        this.datafile = df;
    }

    /**
     * StructuredTextFile needs a root FolderNode that symbolizes a record.
     * Implementations of StructuredTextFile supply this by overriding this
     * method. The root node should be the only one that has no parent.
     *
     * @return
     */
    public abstract FolderNode createRoot();

    public void openRead() {
        checkFirstUse();
        if (datafile != null) {
            if (reader == null) {
                reader = datafile.rwbuffer;
            }
            datafile.openRead();
            if (hasHeader) {
                ByteSearchSection section = reader.readSectionStart(root.section);
                if (section.notEmpty()) {
                    root.readHeader(section);
                    reader.movePast(section);
                }
            }
        }
    }

    public void dumpStructure() {
        log.trace("%s", root.dumpStructure());
    }
    
    public void openWrite() {
        checkFirstUse();
        if (root.writeenabled() && datafile != null) {
            datafile.openWrite();
            if (hasHeader) {
                root.writeHeader();
            }
        }
    }

    public void closeRead() {
        if (datafile != null) {
            datafile.closeRead();
        }
    }

    public void hasHeader() {
        hasHeader = true;
    }

    public void closeWrite() {
        if (datafile != null) {
            datafile.closeWrite();
        }
    }

    public void resetStart() {
        if (datafile != null) {
            datafile.resetStart();
        }
    }

    public void checkFirstUse() {
        if (firstUse) {
            rebuildBeforeFirstUse();
            firstUse = false;
        }
    }

    public long getLength() {
        return datafile.getLength();
    }

    public void delete() {
        datafile.delete();
    }

    /**
     * Override this to change the structure before the first use, e.g. for TSV,
     * normal fields are terminated with a \t but after the last field this may
     * be omitted.
     */
    public void rebuildBeforeFirstUse() {
    }

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
            throw new RuntimeException(PrintTools.sprintf("Should lock file before append %s", datafile.getName()));
        }
        datafile.openAppend();
    }

    public long getOffset() {
        return datafile.getOffset();
    }

    public long getCeiling() {
        return datafile.getCeiling();
    }

    public void setCeiling(long ceiling) {
        datafile.setCeiling(ceiling);
    }

    public void setOffset(long offset) {
        datafile.setOffset(offset);
    }

    public void setBufferSize(int buffersize) {
        datafile.setBufferSize(buffersize);
    }

    public boolean hasMore() {
        return datafile.hasMore();
    }

    public boolean findFirstRecord() {
        if (getOffset() == 0 && getOffset() < getCeiling()) {
            return true;
        }
        ByteSearchPosition matchPos = datafile.matchPos(root.section);
        if (matchPos.found()) {
            datafile.movePast(matchPos);
            return getOffset() < getCeiling();
        }
        return false;
    }

    public boolean nextRecord() {
        try {
            while (true) {
                ByteSearchSection section = reader.readSectionStart(root.section);
                if (validRecord(section)) {
                    root.emptyDataContainer();
                    root.readNode(section);
                    reader.movePast(section);
                    return true;
                }
                reader.movePast(section);
                if (!reader.hasMore()) {
                    break;
                }
            }
        } catch (EOCException ex) {
        }
        root.emptyDataContainer();
        return false;
    }

    protected boolean validRecord(ByteSearchSection section) {
        return section.notEmpty();
    }

    public void read() {
        openRead();
        try {
            while (true) {
                ByteSearchSection section = reader.readSectionStart(root.section);
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
        return section.findAllSections(needle);
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

        protected abstract String dumpStructure();
        
        protected abstract void emptyDataContainer();

        protected abstract void write(ArrayList values);

        protected abstract void writeHeader();

        protected abstract void readNode(ByteSearchSection section);

        protected abstract void readHeader(ByteSearchSection section);

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

        protected String dumpStructure() {
            return getClass().getSimpleName() + ":" + this.label;
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
            set(value);
        }

        @Override
        protected void readHeader(ByteSearchSection outersection) {
        }

        @Override
        protected void write(ArrayList list) {
            for (T t : (ArrayList<T>) list) {
                datafile.printf("%s%s%s", openlabel, toString(t), closelabel);
            }
        }

        @Override
        protected void writeHeader() {
            datafile.printf("%s%s%s", openlabel, label, closelabel);
        }

        public T get() {
            ArrayList<T> list = parent.get(this);
            return (list != null && list.size() > 0) ? list.get(0) : null;
        }

        public T get(NodeValue parentvalue) {
            Object v = parentvalue.get(this);
            return (v != null) ? (T) v : null;
        }

        private ArrayList<T> getList(NodeValue parentvalue) {
            return parentvalue.getListData(this);
        }

        private ArrayList<T> getList() {
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
            if (nestedfields.containsKey(f.label)) {
                log.fatal("cannot use the same label %s in the same FolderNode %s twice", f.label, this.label);
            }
            nestedfields.put(f.label, f);
            orderedfields.add(f);
        }

        protected String dumpStructure() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.label).append(":{ ");
            for (Node n : orderedfields) {
                sb.append(n.dumpStructure()).append(" ");
            }
            return sb.append("}").toString();
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
                //log.info("readNode node %s", f.label);
                for (ByteSearchSection pos : findAllSections(section, f.section)) {
                    //log.info("pos %s", pos.reportString());
                    f.addAnother();
                    f.readNode(pos);
                }
            }
        }

        protected void readHeader(ByteSearchSection section) {
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
        protected void writeHeader() {
            if (openlabel.length() > 0) {
                datafile.printf("%s", openlabel);
            }
            for (Node f : orderedfields) {
                f.writeHeader();
            }
            if (closelabel.length() > 0) {
                datafile.printf("%s", closelabel);
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
            if (section.notEmpty()) {
                return Double.parseDouble(stringValue(section));
            } else {
                return 0.0;
            }
        }

        public String toString(Double value) {
            return value.toString();
        }
    }

    public class IntField extends DataNode<Integer> {

        protected IntField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        public IntField(String label, String open, String close, String openlabel, String closelabel) {
            this(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
        }

        public IntField(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
            this(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
        }

        @Override
        public Integer value(ByteSearchSection section) {
            if (section.notEmpty()) {
                try {
                    return Integer.parseInt(stringValue(section));
                } catch (NumberFormatException ex) {
                    log.fatalexception(ex, "value() offset %d section %s", StructuredTextFile.this.datafile.getOffset(), section.reportString());
                }
            }
            return 0;
        }

        public String toString(Integer value) {
            return value.toString();
        }
    }

    public class IntArrayField extends DataNode<int[]> {

        protected IntArrayField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public int[] value(ByteSearchSection section) {
            int[] result = new int[0];
            if (section.notEmpty()) {
                String stringvalue = stringValue(section);
                if (stringvalue.length() > 0) {
                    try {
                        String part[] = stringvalue.split(",");
                        result = new int[part.length];
                        for (int i = 0; i < part.length; i++) {
                            result[i] = Integer.parseInt(part[i]);
                        }
                    } catch (NumberFormatException ex) {
                        log.fatalexception(ex, "value() offset %d section %s", StructuredTextFile.this.datafile.getOffset(), section.reportString());
                    }
                }
            }
            return result;
        }

        @Override
        public String toString(int[] value) {
            return ArrayTools.toString(value, 0, value.length, ",");
        }
    }

    public class LongArrayField extends DataNode<long[]> {

        protected LongArrayField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public long[] value(ByteSearchSection section) {
            long[] result = ArrayTools.emptyLongArray;
            if (section.notEmpty()) {
                String stringvalue = stringValue(section);
                if (stringvalue.length() > 0) {
                    try {
                        String part[] = stringvalue.split(",");
                        result = new long[part.length];
                        for (int i = 0; i < part.length; i++) {
                            result[i] = Long.parseLong(part[i]);
                        }
                    } catch (NumberFormatException ex) {
                        log.fatalexception(ex, "value() offset %d section %s", StructuredTextFile.this.datafile.getOffset(), section.reportString());
                    }
                }
            }
            return result;
        }

        @Override
        public String toString(long[] value) {
            return ArrayTools.toString(value, 0, value.length, ",");
        }
    }

    public class Long128ArrayField extends DataNode<Long128[]> {

        protected Long128ArrayField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public Long128[] value(ByteSearchSection section) {
            Long128[] result = new Long128[0];
            if (section.notEmpty()) {
                String stringvalue = stringValue(section);
                if (stringvalue.length() > 0) {
                    try {
                        String part[] = stringvalue.split(",");
                        result = new Long128[part.length];
                        for (int i = 0; i < part.length; i++) {
                            result[i] = new Long128(part[i]);
                        }
                    } catch (NumberFormatException ex) {
                        log.fatalexception(ex, "value() offset %d section %s", StructuredTextFile.this.datafile.getOffset(), section.reportString());
                    }
                }
            }
            return result;
        }

        @Override
        public String toString(Long128[] value) {
            return ArrayTools.toString(value, 0, value.length, ",");
        }
    }

    public class DoubleArrayField extends DataNode<double[]> {

        protected DoubleArrayField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public double[] value(ByteSearchSection section) {
            double[] result = ArrayTools.emptyDoubleArray;
            if (section.notEmpty()) {
                String stringvalue = stringValue(section);
                if (stringvalue.length() > 0) {
                    try {
                        String part[] = stringvalue.split(",");
                        result = new double[part.length];
                        for (int i = 0; i < part.length; i++) {
                            result[i] = Double.parseDouble(part[i]);
                        }
                    } catch (NumberFormatException ex) {
                        log.fatalexception(ex, "value() offset %d section %s", StructuredTextFile.this.datafile.getOffset(), section.reportString());
                    }
                }
            }
            return result;
        }

        @Override
        public String toString(double[] value) {
            return ArrayTools.toString(value, 0, value.length, ",");
        }
    }

    public class StringArrayField extends DataNode<String[]> {

        protected StringArrayField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public String[] value(ByteSearchSection section) {
            if (section.notEmpty()) {
                return stringValue(section).split(",");
            } else {
                return new String[0];
            }
        }

        @Override
        public String toString(String[] value) {
            String result = ArrayTools.toString(value, 0, value.length, ",");
            if (StrTools.countIndexOf(result, ',') != value.length - 1) {
                log.fatal("StringArrayField cannot contain , in '%s'", result);
            }
            return result;
        }
    }

    public class LongField extends DataNode<Long> {

        protected LongField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public Long value(ByteSearchSection section) {
            if (section.notEmpty()) {
                return Long.parseLong(stringValue(section));
            } else {
                return 0l;
            }
        }

        public String toString(Long value) {
            return value.toString();
        }
    }

    public class Long128Field extends DataNode<Long128> {

        protected Long128Field(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public Long128 value(ByteSearchSection section) {
            if (section.notEmpty()) {
                return new Long128(stringValue(section));
            } else {
                return null;
            }
        }

        public String toString(Long128 value) {
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
            return value != null ? value : "";
        }
    }

    private static Gson gson = new Gson();

    public class JsonField extends DataNode<JsonObject> {

        private ByteSearch open;
        private ByteSearch close;
        private boolean checked = false;

        protected JsonField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
            if (open != ByteSearch.EMPTY) {
                this.open = open;
            }
            if (close != ByteSearch.EMPTY) {
                this.close = close;
            }
        }

        @Override
        public JsonObject value(ByteSearchSection section) {
            if (section.notEmpty()) {
                return gson.fromJson(this.stringValue(section), JsonObject.class);
            } else {
                return new JsonObject();
            }
        }

        @Override
        public String toString(JsonObject value) {
            if (value != null) {
                String v = gson.toJson(value);
                check(v);
                return v;
            }
            return "";
        }

        private void check(String content) {
            if (!checked && open != null && open != ByteSearch.EMPTY) {
                if (open.match(content)) {
                    log.fatalexception(new RuntimeException(), "StructuredTextFile.JsonField value matches open tag: [%s]\n%s", open.toString(), content);
                }
                if (close != null && close.exists(content)) {
                    ByteSearchPosition findPos = close.findPos(content);
                    if (findPos.notEmpty()) {
                        log.fatalexception(new RuntimeException(), "StructuredTextFile.JsonField value matches close tag: [%s]\n%s", close.toString(), content);
                    }
                }
                checked = true;
            }
        }

        @Override
        public void set(JsonObject o) {
            checked = false;
            super.set(o);
        }

        public void set(String s) {
            check(s);
            set(gson.fromJson(s, JsonObject.class));
        }
    }

    public class JsonArrayField<T> extends DataNode<ArrayList<T>> {

        private ByteSearch open;
        private ByteSearch close;
        private Type genericType;
        private boolean checked = false;

        protected JsonArrayField(FolderNode parent, String label, Type genericType, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
            this.genericType = genericType;
            if (open != ByteSearch.EMPTY) {
                this.open = open;
            }
            if (close != ByteSearch.EMPTY) {
                this.close = close;
            }
        }

        @Override
        public ArrayList<T> value(ByteSearchSection section) {
            if (section.notEmpty()) {
                String stringValue = this.stringValue(section);
                //log.info("JsonArray %s", stringValue);
                ArrayList<T> result;
                try {
                    result = (ArrayList<T>) gson.fromJson(stringValue, genericType);
                } catch (JsonSyntaxException ex) {
                    log.info("failing %s", stringValue);
                    throw ex;
                }
                return result;
            } else {
                return new ArrayList<T>();
            }
        }

        @Override
        public String toString(ArrayList<T> values) {
            if (value != null) {
                String v = gson.toJson(values);
                check(v);
                return v;
            }
            return "";
        }

        private void check(String content) {
            if (!checked) {
                if (open != null && open != ByteSearch.EMPTY && open.match(content)) {
                    log.fatalexception(new RuntimeException(), "StructuredTextFile.JsonArrayField value matches open tag: [%s]\n%s", open.toString(), content);
                }
                if (close != null && close != ByteSearch.EMPTY && close.exists(content)) {
                    ByteSearchPosition findPos = close.findPos(content);
                    if (findPos.notEmpty()) {
                        log.fatalexception(new RuntimeException(), "StructuredTextFile.JsonArrayField value matches close tag: [%s]\n%s", close.toString(), content);
                    }
                }
                checked = true;
            }
        }

        @Override
        public void set(ArrayList<T> o) {
            checked = false;
            super.set(o);
        }
    }

    public class BoolField extends DataNode<Boolean> {

        protected BoolField(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
            super(parent, label, open, close, openlabel, closelabel);
        }

        @Override
        public Boolean value(ByteSearchSection section) {
            if (section.notEmpty()) {
                return Boolean.parseBoolean(this.stringValue(section));
            } else {
                return false;
            }
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

    public Long128Field addLong128(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new Long128Field(parent, label, open, close, openlabel, closelabel);
    }

    public Long128Field addLong128(String label, String open, String close, String openlabel, String closelabel) {
        return addLong128(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public Long128Field addLong128(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addLong128(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
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

    public StringArrayField addStringArray(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new StringArrayField(parent, label, open, close, openlabel, closelabel);
    }

    public StringArrayField addStringArray(String label, String open, String close, String openlabel, String closelabel) {
        return addStringArray(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public StringArrayField addStringArray(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addStringArray(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public IntArrayField addIntArray(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new IntArrayField(parent, label, open, close, openlabel, closelabel);
    }

    public IntArrayField addIntArray(String label, String open, String close, String openlabel, String closelabel) {
        return addIntArray(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public IntArrayField addIntArray(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addIntArray(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public LongArrayField addLongArray(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new LongArrayField(parent, label, open, close, openlabel, closelabel);
    }

    public LongArrayField addLongArray(String label, String open, String close, String openlabel, String closelabel) {
        return addLongArray(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public LongArrayField addLongArray(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addLongArray(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public Long128ArrayField addLong128Array(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new Long128ArrayField(parent, label, open, close, openlabel, closelabel);
    }

    public Long128ArrayField addLong128Array(String label, String open, String close, String openlabel, String closelabel) {
        return addLong128Array(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public Long128ArrayField addLong128Array(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addLong128Array(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public DoubleArrayField addDoubleArray(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new DoubleArrayField(parent, label, open, close, openlabel, closelabel);
    }

    public DoubleArrayField addDoubleArray(String label, String open, String close, String openlabel, String closelabel) {
        return addDoubleArray(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public DoubleArrayField addDoubleArray(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addDoubleArray(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public JsonField addJson(FolderNode parent, String label, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new JsonField(parent, label, open, close, openlabel, closelabel);
    }

    public JsonField addJson(String label, String open, String close, String openlabel, String closelabel) {
        return addJson(getRoot(), label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public JsonField addJson(FolderNode parent, String label, String open, String close, String openlabel, String closelabel) {
        return addJson(parent, label, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public JsonArrayField addJsonArray(FolderNode parent, String label, Type clazz, ByteSearch open, ByteSearch close, String openlabel, String closelabel) {
        return new JsonArrayField(parent, label, clazz, open, close, openlabel, closelabel);
    }

    public JsonArrayField addJsonArray(String label, Type clazz, String open, String close, String openlabel, String closelabel) {
        return addJsonArray(getRoot(), label, clazz, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
    }

    public JsonArrayField addJsonArray(FolderNode parent, String label, Type clazz, String open, String close, String openlabel, String closelabel) {
        return addJsonArray(parent, label, clazz, ByteSearch.create(open), ByteSearch.create(close), openlabel, closelabel);
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
