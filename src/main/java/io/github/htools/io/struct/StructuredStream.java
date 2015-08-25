package io.github.htools.io.struct;

import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructureData;
import io.github.htools.lib.Log;

/**
 * Provides streamed data access by iterative read/write actions of records/rows
 * that consists of some defined structure. 
 * <p>
 * @author jeroen
 */
public abstract class StructuredStream {

    public Log log = new Log(StructuredStream.class);
    protected StartField start = addStart();     // starting point to the linked list that contains the structure definition
    protected Field last = start;           // used only for contructing the structure
    Field nextField;                // points to the next element to be read/written
    public StructureWriter writer;  // the output of the Stream
    public StructureReader reader;  // the input of the Stream
    public final static byte emptypattern[] = new byte[0];

    protected StructuredStream() {
    }

    /**
     * Initializes a Structure for reading/writing
     * <p>
     * @param readerwriter The object to Stream the data from or to
     */
    public StructuredStream(StructureData readerwriter) {
        this();
        this.writer = readerwriter;
        this.reader = readerwriter;
    }

    /**
     * Initializes a Structure for reading
     * <p>
     * @param reader The source to stream from
     */
    public StructuredStream(StructureReader reader) {
        this();
        this.reader = reader;
    }

    /**
     * Initializes a Structure for writing
     * <p>
     * @param writer The destination to Stream to
     */
    public StructuredStream(StructureWriter writer) {
        this();
        this.writer = writer;
    }

    private void setStructurePos(Field e) {
        for (nextField = start; nextField != null && nextField != e; nextField = nextField.next);
        if (nextField == null) {
            log.fatal("Trying to move to an unknown label %s in %s", e.label, this.toString());
        }
    }

    private void setStructurePos(String e) {
        for (nextField = start; nextField != null && !e.equals(nextField.label); nextField = nextField.next);
        if (nextField == null) {
            log.fatal("Trying to move to an unknown label %s in %s", e, this.toString());
        }
    }

    /**
     * @return the next Field to be read or written
     */
    public Field getNextField() {
        return nextField;
    }

    /**
     * @return true if nextField is the first field in the structure definition
     */
    public boolean isAtStart() {
        return (nextField == start.next);
    }

    /**
     * sets nextField to the first field in the structure definition
     */
    public void resetNextField() {
        nextField = start.next;
    }

    public void add(Field e) {
        last.next = e;
        last = e;
    }

    /**
     * Removes a field from the structure, which is useful if the structure uses
     * additional fields upon construction, that are left out in the final data
     * stream, such as sort codes.
     * <p>
     * @param removeField
     */
    protected void remove(Field removeField) {
        if (removeField == this.nextField) {
            advancePos();
        }
        Field f = start;
        for (; f != null && f.next != removeField; f = f.next);
        if (f != null) {
            f.next = f.next.next;
        }
    }

    /**
     * finds a field with a given type and label, used internally to close
     * IterFields
     * <p>
     * @param type
     * @param label
     * @return the first field with corresponding type and label, or null if not
     * found.
     */
    protected Field find(FieldType type, String label) {
        Field e = start;
        for (; e != null && (e.type != type || !e.label.equals(label)); e = e.next);
        return e;
    }

    protected StartField addStart() {
        return new StartField();
    }

    protected String getCurrentLabel() {
        if (nextField != null) {
            return nextField.label + " " + nextField.type.toString();
        }
        return "END";
    }

    protected void checkRead(Field e) {
        if (nextField == null) {
            log.fatal("fatal end of StructuredFile reached trying to read/write %s in %s",
                    e.type.toString(), this.toString());
        }
        if (nextField != e) {
            log.fatal("Invalid Structure trying to read/write data labelled %s in %s", e.label, this.toString());
        }
        if (e.type == FieldType.ITERSTART || e.type == FieldType.LONGITERSTART) {
            return;
        }
        advancePos();
    }

    protected void checkWrite(Field e) {
        if (nextField == null) {
            log.fatal("fatal end of StructuredFile reached trying to read/write %s in %s",
                    e.type.toString(), this.toString());
        }
        if (nextField != e) {
            log.fatal("Invalid Structure trying to read/write data labelled %s in %s", e.label, this.toString());
        }
        if (writer != null) {
            e.lastoffset = writer.getOffset();
        }
        hookCheckWrite(e);
        if (e.type == FieldType.ITERSTART || e.type == FieldType.LONGITERSTART) {
            return;
        }
        advancePos();
    }

    /**
     * provides a hook after checkWrite and before write is executed
     * <p>
     * @param nextField the field that is about to be written
     */
    public void hookCheckWrite(Field nextField) {
    }

    protected void advancePos() {
        nextField = nextField.nextField();
        if (nextField == null) {
            resetNextField();
        }
        posMoved();
    }

    /**
     * provides a hook after the position was moved to the nextField
     */
    public void posMoved() {
    }

    /**
     * provides a hook after a field was written
     */
    public void writeDone(Field f) {
    }

//   @Override
//   public String toString() {
//      StringBuilder sb = new StringBuilder();
//      sb.append("");
//      sb.append(" { ");
//      for (Field e = start; e != null; e = e.next) {
//         if (e == nextField) {
//            sb.append("[ ");
//         }
//         sb.append(e.label[0]);
//         sb.append(":");
//         sb.append(e.type.toString());
//         sb.append(" ");
//         if (e == nextField) {
//            sb.append("] ");
//         }
//      }
//      sb.append("}");
//      return sb.toString();
//   }
    public static enum FieldType {

        BOOLEAN,
        INT,
        INT2,
        INT3,
        CINT,
        INTARRAY,
        CINTARRAY2,
        SQUAREDINTARRAY2,
        INTSPARSE2,
        SQUAREDINTARRAY3,
        CINTARRAY3,
        INTSPARSE3,
        CINTARRAY,
        CINTINCR,
        BOOLARRAY,
        LONG,
        CLONG,
        LONGARRAY,
        CLONGARRAY2,
        LONGSPARSE2,
        CLONGARRAY,
        LONGSPARSE,
        INTSPARSE,
        DOUBLESPARSE,
        DOUBLEARRAY,
        DOUBLE,
        CDOUBLE,
        BYTE,
        FIXEDMEM,
        MEM,
        UNSIGNEDBYTE,
        SIGNATURE,
        STRING,
        STRING0,
        STRINGPAIRARRAY,
        START,
        STRINGARRAY,
        ITERSTART,
        LONGITERSTART,
        LONGITEREND,
        ITEREND
    }

    /**
     * The building brick of a structure definition. Fields of any type
     * extending Field can be added to the structure. Values can be written/read
     * using the fields through their local read/write methods. Each field
     * should have a value variable that contains the last value written/read.
     */
    protected abstract class Field {

        FieldType type;
        public String label;
        long lastoffset;
        Field next;

        Field() {
        }

        protected Field(FieldType type, String label) {
            this.type = type;
            this.label = label;
            add(this);
            resetNextField();
        }

        public boolean isFirst() {
            return this == start.next;
        }

        public boolean isLast() {
            return next == null;
        }

        /**
         * set nextField to this field
         */
        public void setStructurePos() {
            StructuredStream.this.setStructurePos(this);
        }

        /**
         * @return the field that comes after this in the structure sequence, or
         * null if no such field exists.
         */
        public Field nextField() {
            if (next != null) {
                return next.arrivingAt();
            }
            return null;
        }

        protected Field arrivingAt() {
            return this;
        }

        /**
         * reads the Field from the Stream, storing the read values in the value
         * class variable of the Field.
         * <p>
         * @throws EOCException when EOF or ceiling is encountered
         */
        public abstract void readNoReturn() throws EOCException;

        public abstract void write() throws EOCException;

        /**
         * skips reading this field and moves the offset to the next Field
         * <p>
         * @throws EOCException when EOF or ceiling is encountered,
         */
        public abstract void skip() throws EOCException;

        /**
         * @return the fields label, to identify an anonymous field, mainly used
         * for debug purposes.
         */
        public String getLabel() {
            return label;
        }

        public abstract StructuredStream.Field clone(StructuredStream d);
    }

    protected class StartField extends Field {

        protected StartField() {
            this.type = FieldType.START;
            this.label = "StartField";
            last = this;
            last.next = this;
        }

        public void readNoReturn() throws EOCException {
            throw new EOCException("StartField should never be read");
        }

        public void skip() throws EOCException {
            throw new EOCException("StartField should never be read");
        }

        public void write() {
            throw new UnsupportedOperationException();
        }

        public Field clone(StructuredStream d) {
            return d.addStart();
        }
    }
}
