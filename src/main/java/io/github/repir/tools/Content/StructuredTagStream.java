package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Provides streamed data access by iterative read/write actions of records/rows
 * that consists of some defined structure. This class is intended to read/write
 * content to flat text files, so the data can be read/written/changed outside
 * the program.
 * <p/>
 * To facilitate writing data to flat files, a field separator is required. By
 * default the row separator is a newline and the field separator is a space.
 * This means String containing these characters will be escaped by an escape
 * character which is a backslash.
 * <p/>
 * @author jeroen
 */
public abstract class StructuredTagStream extends StructuredStream {

   public Log log = new Log(StructuredTagStream.class);

   protected StructuredTagStream() {
      super();
   }

   /**
    * creates the bytes to insert before printing the field. This could be
    * overridden for XML style files, to write a start tag, or if
    * {@link Field#isFirst()} is true, to write start row tag + start tag.
    * <p/>
    * @param f The Field that requires the start tag.
    * @return a byte array that contains the start tag for this field.
    */
   public byte[] createStartSingleTag(Field f) {
      return ("  <" + f.label[0] + " >").getBytes();
   }

   /**
    * creates the bytes to insert at the start of a record. This could be
    * overridden for XML style files, to write a record tag.
    * <p/>
    * @param f The Field that requests the record tag.
    * @return a byte array that contains the record start tag if this is the
    * first field, otherwise an empty array.
    */
   public byte[] createStartRecord(Field f) {
      return "<row >\n".getBytes();
   }

   /**
    * creates the bytes to insert at the end of a record. This could be
    * overridden for XML style files, to finish a record tag.
    * <p/>
    * @param f The Field that requests the record tag.
    * @return a byte array that contains the record end tag if this is the last
    * field, otherwise an empty array.
    */
   public byte[] createEndRecord(Field f) {
      return "</row >\n".getBytes();
   }

   /**
    * creates the bytes to check if a record should be skipped. This is for
    * instance used for in CSV Records, because Fields have no start tag to
    * identify them.
    * <p/>
    * @param f The Field that requests the record tag.
    * @return a byte array that indicates there is no record.
    */
   public byte[] createPeekEndField(Field f) {
      return emptypattern;
   }

   /**
    * creates the bytes to insert after printing the field. This could be
    * overridden for XML style files, to write an end tag, or if
    * {@link Field#isLast()} is true, to write end tag + end row tag.
    * <p/>
    * @param f The Field that requires the end tag.
    * @return a byte array that contains the end tag for this field.
    */
   public byte[] createEndSingleTag(Field f) {
      return ("</" + f.label[0] + " >\n").getBytes();
   }

   public byte[] createArrayStartTag(ArrayField f) {
      return (f.label[1] == null || f.label[1].length() == 0) ? emptypattern : ("  <" + f.label[1] + " >\n").getBytes();
   }

   public byte[] createArrayEndTag(ArrayField f) {
      return (f.label[1] == null || f.label[1].length() == 0) ? emptypattern
              : ("  </" + f.label[1] + " >\n").getBytes();
   }

   public byte createEscape(Field f) {
      return '\\';
   }

   protected String readString(byte pattern[], byte escape) throws EOFException {
      //log.info("read %s", new String(pattern));
      return reader.readStringWS(pattern, escape);
   }

   protected String readString(byte pattern[], byte peekend[], byte escape) throws EOFException {
      String a = reader.readStringWS(pattern, peekend, escape);
      //log.info("read '%s'", a);
      return a;
   }

   protected boolean peekStringExists(byte pattern[]) throws EOFException {
      return reader.peekStringExistsWS(pattern);
   }

   protected boolean peekStringNotExists(byte pattern[]) throws EOFException {
      return reader.peekStringNotExistsWS(pattern);
   }

   protected void skipString(byte pattern[], byte escape) throws EOFException {
      reader.skipStringWS(pattern, escape);
   }

   protected void skipString(byte pattern[], byte peekend[], byte escape) throws EOFException {
      reader.skipStringWS(pattern, peekend, escape);
   }

   protected void startRead(Field f) throws EOFException {
      //log.info("startRead %d", reader.getOffset());
      checkRead(f);
      if (f.isFirst() && f.startrecord.length > 0) {
         String v = readString(f.startrecord, f.escape);
         if (v.length() > 0) {
            log.fatal("illegal characters before field %s: '%s'", f.label[0], v);
         }
      }
      //log.info("end startRead %d", reader.getOffset());
   }

   protected void endRead(Field f) throws EOFException {
      //log.info("endRead %s %b '%s'", f.label[0], f.isLast(), new String(f.endrecord));
      if (f.isLast() && f.endrecord.length > 0) {
         String v = readString(f.endrecord, f.escape);
         if (v.length() > 0) {
            log.info("Record may not be properly terminated '%s'", v);
         }
      }
   }

   protected String readSingle(Field f) throws EOFException {
      //log.info("readSingle() strart %s", f.label[0]);
      startRead(f);
      if (f.startfield.length > 0) {
         if (peekStringNotExists(f.startfield)) {
            //log.fatal("nostartfield( %s )", new String( f.startfield ));
            endRead(f);
            return "";
         }
         readString(f.startfield, f.escape);
      } else if (!peekStringNotExists(f.peekendfield)) {
         //log.fatal("peekStringNotExists( %s )", new String( f.peekendfield ));
         endRead(f);
         return "";
      }
      //log.info("aap");
      String v = readString(f.endfield, f.peekendfield, f.escape);
      //log.info("aap");
      endRead(f);
      //log.info("readSingle end '%s' %d", v, reader.getOffset());
      return v;
   }

   public ArrayList<String> readArray(ArrayField f) throws EOFException {
      startRead(f);
      ArrayList<String> al = new ArrayList<String>();
      if ((peekStringNotExists(f.peekendfield)) && peekStringExists(f.arrayopen)) {
         skipString(f.arrayopen, f.escape);
         //log.info("startarray '%s' startfield '%s' endfield '%s' endarray '%s'", new String(f.arrayopen), new String(f.startfield), new String(f.endfield), new String(f.arrayclose));
         while (peekStringExists(f.startfield) && (peekStringNotExists(f.arrayclose))) {
            skipString(f.startfield, f.escape);
            al.add(readString(f.endfield, f.peekendfield, f.escape));
         }
         if (f.arrayclose.length > 0) {
            skipString(f.arrayclose, f.peekendfield, f.escape);
         }
      }
      endRead(f);
      return al;
   }

   public void skipArray(ArrayField f) throws EOFException {
      startRead(f);
      if ((f.peekendfield.length == 0 || !peekStringExists(f.peekendfield))
              && (f.arrayopen.length == 0 || this.peekStringExists(f.arrayopen))) {
         skipString(f.arrayopen, f.escape);
         while (peekStringExists(f.startfield) && (f.arrayclose.length == 0 || !peekStringExists(f.arrayclose))) {
            skipString(f.startfield, f.escape);
            skipString(f.endfield, f.peekendfield, f.escape);
         }
         if (f.arrayclose.length > 0) {
            skipString(f.arrayclose, f.escape);
         }
      }
      endRead(f);
   }

   public void writeArray(ArrayField f, String al[]) {
      startWrite(f);
      if (al.length > 0) {
         //log.info("%d %s", f.arrayopen.length, new String(f.arrayopen));
         writer.write(f.arrayopen);
         int i = 0;
         for (String s : al) {
            writer.write(f.startfield);
            writer.write(s.getBytes());
            writer.write(f.endfield);
         }
         writer.write(f.arrayclose);
      }
      endWrite(f);
   }

   protected void skipSingle(Field f) throws EOFException {
      startRead(f);
      if (f.startfield.length > 0) {
         if (peekStringNotExists(f.peekendfield) && peekStringExists(f.startfield)) {
            skipString(f.startfield, f.peekendfield, f.escape);
            skipString(f.endfield, f.escape);
         }
      } else {
         skipString(f.endfield, f.escape);
      }
      endRead(f);
   }

   protected void startWrite(Field f) {
      checkWrite(f);
      if (f.isFirst()) {
         writer.write(f.startrecord);
      }
   }

   protected void endWrite(Field f) {
      if (f.isLast()) {
         writer.write(f.endrecord);
         writeDone(f);
      }
   }

   protected void writeSingle(Field f, String v) {
      startWrite(f);
      if (v != null) {
         writer.write(f.startfield);
         writer.write(v.getBytes(), f.endfield, f.peekendfield, f.escape);
         writer.write(f.endfield);
      }
      endWrite(f);
   }

   /**
    * Initializes a Structure for reading/writing
    * <p/>
    * @param readerwriter The object to Stream the data from or to
    */
   public StructuredTagStream(StructureData readerwriter) {
      super(readerwriter);
   }

   /**
    * Initializes a Structure for reading
    * <p/>
    * @param reader The source to stream from
    */
   public StructuredTagStream(StructureReader reader) {
      super(reader);
   }

   /**
    * Initializes a Structure for writing
    * <p/>
    * @param writer The destination to Stream to
    */
   public StructuredTagStream(StructureWriter writer) {
      super(writer);
   }

   /**
    * add an int
    * <p/>
    * @param label unique name of the Field
    * @return the added IntField
    */
   public IntField addInt(String label) {
      return new IntField(label);
   }

   /**
    * add a double
    * <p/>
    * @param label unique name of the Field
    * @return the added DoubleField
    */
   public DoubleField addDouble(String label) {
      return new DoubleField(label);
   }

   /**
    * add a String
    * <p/>
    * @param label unique name of the Field
    * @return the added StringField
    */
   public StringField addString(String label) {
      return new StringField(label);
   }

   /**
    * add an array of Strings
    * <p/>
    * @param label unique name of the Field
    * @return the added StringArrayField
    */
   public StringArrayField addStringArray(String... label) {
      return new StringArrayField(label);
   }

   /**
    * add an array of ints
    * <p/>
    * @param label unique name of the Field
    * @return the added IntArrayField
    */
   public IntArrayField addIntArray(String... label) {
      return new IntArrayField(label);
   }

   /**
    * add an array of Longs.
    * <p/>
    * @param label unique name of the Field
    * @return the added LongArray2Field
    */
   public LongArrayField addLongArray(String... label) {
      return new LongArrayField(label);
   }

   /**
    * add an array of Longs.
    * <p/>
    * @param label unique name of the Field
    * @return the added LongArray2Field
    */
   public DoubleArrayField addDoubleArray(String... label) {
      return new DoubleArrayField(label);
   }

   /**
    * add a signed 1-byte integer
    * <p/>
    * @param label unique name of the Field
    * @return the added ByteField
    */
   public ByteField addByte(String label) {
      return new ByteField(label);
   }

   /**
    * add a Long
    * <p/>
    * @param label unique name of the Field
    * @return the added LongField
    */
   public LongField addLong(String label) {
      return new LongField(label);
   }

   @Override
   public void add(StructuredStream.Field e) {
      //log.info("add( %s )", e.label[0]);
      if (last instanceof Field) {
         ((Field) last).endrecord = emptypattern;
      }
      super.add(e);
   }

   public abstract class Field extends StructuredStream.Field {

      public byte startrecord[];
      public byte endrecord[];
      public byte startfield[];
      public byte endfield[];
      public byte peekendfield[];
      public byte escape;

      public Field(FieldType type, String... label) {
         super(type, label);
         startfield = createStartSingleTag(this);
         endfield = createEndSingleTag(this);
         startrecord = (start.next == this) ? createStartRecord(this) : emptypattern;
         endrecord = createEndRecord(this);
         peekendfield = createPeekEndField(this);
         escape = createEscape(this);
      }

      public abstract boolean isArray();

      public boolean evaluateReadEnd() {
         return true;
      }
   }

   public abstract class SingleField extends Field {

      public SingleField(FieldType type, String... label) {
         super(type, label);
      }

      public boolean isArray() {
         return false;
      }

      public void skip() throws EOFException {
         skipSingle(this);
      }
   }

   public abstract class ArrayField extends Field {

      byte arrayopen[];
      byte arrayclose[];

      public ArrayField(FieldType type, String... label) {
         super(type, label);
         arrayopen = createArrayStartTag(this);
         arrayclose = createArrayEndTag(this);
      }

      public boolean isArray() {
         return true;
      }

      public void skip() throws EOFException {
         skipArray(this);
      }
   }

   /**
    * A Field that holds a 4-byte integer.
    */
   public class IntField extends SingleField {

      public int value;

      protected IntField(String label) {
         super(FieldType.INT, label);
      }

      public int read() throws EOFException {
         String v = readSingle(this).trim();
         if (v == null || v.length() == 0) {
            value = 0;
         } else {
            value = Integer.parseInt(v);
         }
         return value;
      }

      public void write(int i) {
         value = i;
         writeSingle(this, Integer.toString(i));
      }

      public StructuredStream.Field clone(StructuredStream d) {
         return ((StructuredTagStream) d).addInt(label[0]);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }

   /**
    * A Field that contains one Double value.
    */
   public class DoubleField extends SingleField {

      public double value;

      protected DoubleField(String label) {
         super(FieldType.DOUBLE, label);
      }

      public double read() throws EOFException {
         String v = readSingle(this);
         if (v == null || v.length() == 0) {
            value = 0;
         } else {
            value = Double.parseDouble(v);
         }
         return value;
      }

      public void write(double d) {
         value = d;
         writeSingle(this, Double.toString(d));
      }

      public StructuredStream.Field clone(StructuredStream d) {
         return ((StructuredTagStream) d).addDouble(label[0]);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }

   /**
    * A field that contains a signed 1-byte integer
    */
   public class ByteField extends SingleField {

      public int value;

      protected ByteField(String label) {
         super(FieldType.BYTE, label);
      }

      public int read() throws EOFException {
         String v = readSingle(this);
         if (v == null || v.length() == 0) {
            value = 0;
         } else {
            value = Byte.parseByte(v);
         }
         return value;
      }

      public void write(byte d) {
         value = d;
         writeSingle(this, Byte.toString(d));
      }

      public StructuredStream.Field clone(StructuredStream d) {
         return ((StructuredTagStream) d).addByte(label[0]);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }

   /**
    * a Field that holds one String. Although the storage is implemented through
    * {@link StructureReader} and {@link StructureWriter}, the intention is that
    * the String is stored with an Integer that contains the string length
    * preceding the string. This enables fast skipping through Strings, although
    * this may not work when the String contains Unicode characters.
    */
   public class StringField extends SingleField {

      public String value;

      protected StringField(String label) {
         super(FieldType.STRING, label);
      }

      public String read() throws EOFException {
         value = readSingle(this);
         return value;
      }

      public void write(String s) {
         value = s;
         writeSingle(this, s);
      }

      public StructuredStream.Field clone(StructuredStream d) {
         return ((StructuredTagStream) d).addString(label[0]);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }

   /**
    * field that contains an Array of Strings. Usually, an integer that contains
    * the array size precedes the string data.
    */
   public class StringArrayField extends ArrayField {

      public String[] value;

      protected StringArrayField(String... label) {
         super(FieldType.STRINGARRAY, label);
      }

      public String[] read() throws EOFException {
         ArrayList<String> al = readArray(this);
         value = al.toArray(new String[al.size()]);
         return value;
      }

      public void write(String array[]) {
         value = array;
         writeArray(this, array);
      }

      public StructuredStream.Field clone(StructuredStream d) {
         return ((StructuredTagStream) d).addStringArray(label);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }

   /**
    * a field that contains an array of Longs.
    */
   public class LongArrayField extends ArrayField {

      public long[] value;

      protected LongArrayField(String... label) {
         super(FieldType.LONGARRAY, label);
      }

      public long[] read() throws EOFException {
         ArrayList<String> al = readArray(this);
         value = new long[al.size()];
         for (int i = 0; i < value.length; i++) {
            value[i] = Long.parseLong(al.get(i));
         }
         return value;
      }

      public void write(long array[]) {
         value = array;
         String stringvalues[] = new String[value.length];
         for (int i = 0; i < value.length; i++) {
            stringvalues[i] = Long.toString(value[i]);
         }
         writeArray(this, stringvalues);
      }

      public StructuredStream.Field clone(StructuredStream d) {
         return ((StructuredTagStream) d).addLongArray(label);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }

   /**
    * a field that contains an int array.
    */
   public class IntArrayField extends ArrayField {

      public int[] value;
      public ArrayList<Integer> alvalue;

      protected IntArrayField(String... label) {
         super(FieldType.INTARRAY, label);
      }

      public int[] read() throws EOFException {
         ArrayList<String> al = readArray(this);
         value = new int[al.size()];
         for (int i = 0; i < value.length; i++) {
            value[i] = Integer.parseInt(al.get(i));
         }
         return value;
      }

      public void write(int array[]) {
         value = array;
         String stringvalues[] = new String[value.length];
         for (int i = 0; i < value.length; i++) {
            stringvalues[i] = Integer.toString(value[i]);
         }
         writeArray(this, stringvalues);
      }

      public StructuredStream.Field clone(StructuredStream d) {
         return ((StructuredTagStream) d).addIntArray(label);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }

   /**
    * a field that contains an double array.
    */
   public class DoubleArrayField extends ArrayField {

      public double[] value;
      public ArrayList<Double> alvalue;

      protected DoubleArrayField(String... label) {
         super(FieldType.DOUBLEARRAY, label);
      }

      public double[] read() throws EOFException {
         ArrayList<String> al = readArray(this);
         value = new double[al.size()];
         for (int i = 0; i < value.length; i++) {
            value[i] = Double.parseDouble(al.get(i));
         }
         return value;
      }

      public void write(double array[]) {
         value = array;
         String stringvalues[] = new String[value.length];
         for (int i = 0; i < value.length; i++) {
            stringvalues[i] = Double.toString(value[i]);
         }
         writeArray(this, stringvalues);
      }

      public StructuredStream.Field clone(StructuredStream d) {
         return ((StructuredTagStream) d).addIntArray(label);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }

   /**
    * a field that contains a Long.
    */
   public class LongField extends SingleField {

      public long value;

      protected LongField(String label) {
         super(FieldType.LONG, label);
      }

      public double read() throws EOFException {
         String v = readSingle(this);
         if (v == null || v.length() == 0) {
            value = 0;
         } else {
            value = Long.parseLong(v);
         }
         return value;
      }

      public void write(long d) {
         value = d;
         writeSingle(this, Long.toString(d));
      }

      public StructuredStream.Field clone(StructuredStream d) {
         return ((StructuredTagStream) d).addLong(label[0]);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }
}
