package io.github.repir.tools.Content;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import io.github.repir.tools.Lib.ArrayTools;

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
public abstract class StructuredTagStream2 extends StructuredStream {

   public Log log = new Log(StructuredTagStream2.class);
   private Pattern spaceconverter = Pattern.compile("\\s+");

   protected StructuredTagStream2() {
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
   public String createStartFieldTag(Field f) {
      return " <" + f.label[0] + " >";
   }

   public ByteRegex createStartFieldRegex(Field f) {
      return regexFromTag(createStartFieldTag(f));
   }

   /**
    * creates the bytes to insert at the start of a record. This could be
    * overridden for XML style files, to write a record tag.
    * <p/>
    * @param f The Field that requests the record tag.
    * @return a byte array that contains the record start tag if this is the
    * first field, otherwise an empty array.
    */
   public String createStartRecord(Field f) {
      return "<row >\n";
   }

   public ByteRegex createStartRecordRegex(Field f) {
      return regexFromTag(createStartRecord(f));
   }

   /**
    * creates the bytes to insert at the end of a record. This could be
    * overridden for XML style files, to finish a record tag.
    * <p/>
    * @param f The Field that requests the record tag.
    * @return a byte array that contains the record end tag if this is the last
    * field, otherwise an empty array.
    */
   public String createEndRecord(Field f) {
      return "</row >\n";
   }

   public ByteRegex createEndRecordRegex(Field f) {
      return regexFromTag(createEndRecord(f));
   }

   /**
    * creates the bytes to insert after printing the field. This could be
    * overridden for XML style files, to write an end tag, or if
    * {@link Field#isLast()} is true, to write end tag + end row tag.
    * <p/>
    * @param f The Field that requires the end tag.
    * @return a byte array that contains the end tag for this field.
    */
   public String createEndFieldTag(Field f) {
      return "</" + f.label[0] + " >\n";
   }

   public ByteRegex createEndFieldRegex(Field f) {
      //log.info("%s", createEndFieldTag(f));
      return new ByteRegex(regexFromTag(createEndFieldTag(f)), createEndRecordRegex(f).lookAhead());
   }

   public String createStartArrayTag(ArrayField f) {
      return (f.label[1] == null || f.label[1].length() == 0) ? "" : " <" + f.label[1] + " >\n";
   }

   public ByteRegex createStartArrayRegex(ArrayField f) {
      return regexFromTag(createStartArrayTag(f));
   }

   public String createEndArrayTag(ArrayField f) {
      return (f.label[1] == null || f.label[1].length() == 0) ? ""
              : " </" + f.label[1] + " >\n";
   }

   public ByteRegex createEndArrayRegex(ArrayField f) {
      return new ByteRegex(regexFromTag(createEndArrayTag(f)), createEndRecordRegex(f).lookAhead());
   }

   protected String readString(ByteRegex regex) throws EOFException {
      //log.info("read %s", new String(pattern));
      return unEscapeString(reader.readString(regex));
   }

   protected boolean peekStringExists(ByteRegex regex) throws EOFException {
      return reader.peekStringExists(regex);
   }

   protected boolean peekStringNotExists(ByteRegex regex) throws EOFException {
      return reader.peekStringNotExists(regex);
   }

   protected void skipString(ByteRegex regex) throws EOFException {
      reader.skipString(regex);
   }

   protected ByteRegex regexFromTag(String tag) {
      return new ByteRegex(spaceconverter.matcher(tag).replaceAll("\\\\s*"));
   }

   protected void startRead(Field f) throws EOFException {
      //log.info("startRead %d", reader.getOffset());
      checkRead(f);
      if (f.isFirst() && !f.startrecordregex.isEmpty()) {
         readStartRecord(f.startrecordregex);
      }
      //log.info("end startRead %d", reader.getOffset());
   }

   protected void readStartRecord(ByteRegex regex) throws EOFException {
      String v = readString(regex);
      if (v.length() > 0) {
         log.fatal("illegal characters before field %s: '%s'", regex.pattern, v);
      }
   }

   protected void endRead(Field f) throws EOFException {
      //log.info("endRead %s %b '%s'", f.getLabel(), f.isLast(), f.endrecordregex.pattern);
      if (f.isLast() && !f.endrecordregex.isEmpty()) {
         readEndRecord(f.endrecordregex);
      }
   }

   protected void readEndRecord(ByteRegex regex) throws EOFException {
      String v = readString(regex);
      if (v.length() > 0) {
         log.info("Record %s may not be properly terminated '%s' pos %d", regex.pattern, v, reader.getOffset());
      }
   }

   protected String readSingle(Field f) throws EOFException {
      //log.info("readSingle(%s) start '%s' end '%s' pos %d", f.getLabel(), f.startfieldregex.pattern, f.endfieldregex.pattern, reader.getOffset());
      startRead(f);
      if (!f.startfieldregex.isEmpty()) {
         if (peekStringNotExists(f.startfieldregex)) {
            //log.fatal("nostartfield( %s )", new String(f.startfield));
            endRead(f);
            return "";
         }
         readString(f.startfieldregex);
      }
      //log.info("aap");
      //f.endfieldregex.print();
      //f.endfieldorrecordregex.print();
      String v = readString(f.endfieldregex);
      //log.info("readvalue '%s'", v);
      endRead(f);
      //log.info("readSingle end '%s' %d", v, reader.getOffset());
      return v;
   }

   public ArrayList<String> readArray(ArrayField f) throws EOFException {
      startRead(f);
      ArrayList<String> al = new ArrayList<String>();
      if (peekStringExists(f.arrayopenregex)) {
         skipString(f.arrayopenregex);
         //log.info("startarray '%s' startfield '%s' endfield '%s' endarray '%s'", new String(f.arrayopen), new String(f.startfield), new String(f.endfield), new String(f.arrayclose));
         while (peekStringExists(f.startfieldregex) && (peekStringNotExists(f.arraycloseregex))) {
            skipString(f.startfieldregex);
            al.add(readString(f.endfieldregex));
         }
         if (!f.arraycloseregex.isEmpty()) {
            skipString(f.arraycloseorrecordregex);
         }
      }
      endRead(f);
      return al;
   }

   public void skipArray(ArrayField f) throws EOFException {
      startRead(f);
      if ((f.endrecordregex.isEmpty() || !peekStringExists(f.endrecordregex))
              && (f.arrayopenregex.isEmpty() || this.peekStringExists(f.arrayopenregex))) {
         skipString(f.arrayopenregex);
         while (peekStringExists(f.startfieldregex) && (f.arraycloseregex.isEmpty() || !peekStringExists(f.arraycloseregex))) {
            skipString(f.startfieldregex);
            skipString(f.endfieldregex);
         }
         if (!f.arraycloseregex.isEmpty()) {
            skipString(f.arraycloseregex);
         }
      }
      endRead(f);
   }

   public void writeArray(ArrayField f, String al[]) {
      startWrite(f);
      if (al.length > 0) {
         //log.info("%d %s", f.arrayopen.length, new String(f.arrayopen));
         writer.write(f.arrayopen.getBytes());
         int i = 0;
         for (String s : al) {
            writer.write(f.startfield.getBytes());
            writer.write(escapeString(s.getBytes(), f.endfieldregex, f.arraycloseregex));
            writer.write(f.endfield.getBytes());
         }
         writer.write(f.arrayclose.getBytes());
      }
      endWrite(f);
   }

   protected void skipSingle(Field f) throws EOFException {
      startRead(f);
      if (!f.startfieldregex.isEmpty()) {
         if (peekStringExists(f.startfieldregex)) {
            skipString(f.startfieldregex);
            skipString(f.endfieldregex);
         }
      } else {
         skipString(f.endfieldregex);
      }
      endRead(f);
   }

   protected void startWrite(Field f) {
      checkWrite(f);
      if (f.isFirst()) {
         writer.write(f.startrecord.getBytes());
      }
   }

   protected void endWrite(Field f) {
      if (f.isLast()) {
         writer.write(f.endrecord.getBytes());
         writeDone(f);
      }
   }

   protected void writeSingle(Field f, String v) {
      startWrite(f);
      if (v != null) {
         writer.write(f.startfield.getBytes());
         byte b[] = escapeString(v.getBytes(), f.endfieldregex, f.endrecordregex);
         writer.write(b);
         writer.write(f.endfield.getBytes());
      }
      endWrite(f);
   }

   protected byte[] escapeString(byte buffer[], ByteRegex... regex) {
      TreeSet<Integer> escapepos = new TreeSet<Integer>();
      for (ByteRegex r : regex) {
         if (!r.isEmpty()) {
            ArrayList<Pos> positions = r.findAll(buffer, 0, buffer.length);
            for (Pos p : positions) {
               if (p.found()) {
                  escapepos.add(p.start);
               }
            }
         }
      }
      for (int i = 0; i < buffer.length; i++) {
         if (buffer[i] == '\\') {
            escapepos.add(i);
         }
      }
      if (escapepos.size() == 0) {
         return buffer;
      }
      byte newbuffer[] = new byte[buffer.length + escapepos.size()];
      StringBuilder sb = new StringBuilder();
      int bufferpos = 0;
      int newbufferpos = 0;
      for (int escape : escapepos) {
         int segmentlength = escape - bufferpos;
         System.arraycopy(buffer, bufferpos, newbuffer, newbufferpos, segmentlength);
         newbufferpos += segmentlength;
         bufferpos += segmentlength;
         newbuffer[ newbufferpos++] = '\\';
      }
      System.arraycopy(buffer, bufferpos, newbuffer, newbufferpos, buffer.length - bufferpos);
      return newbuffer;
   }

   protected String unEscapeString(String v) {
      byte buffer[] = v.getBytes();
      TreeSet<Integer> escapepos = new TreeSet<Integer>();
      for (int i = 0; i < buffer.length; i++) {
         if (buffer[i] == '\\') {
            escapepos.add(i);
            i++;
         }
      }
      if (escapepos.size() == 0) {
         return v;
      }
      StringBuilder sb = new StringBuilder();
      int pos = 0;
      for (int escape : escapepos) {
         sb.append(new String(buffer, pos, escape - pos));
         pos = escape + 1;
      }
      return sb.append(new String(buffer, pos, buffer.length - pos)).toString();
   }

   /**
    * Initializes a Structure for reading/writing
    * <p/>
    * @param readerwriter The object to Stream the data from or to
    */
   public StructuredTagStream2(StructureData readerwriter) {
      super(readerwriter);
   }

   /**
    * Initializes a Structure for reading
    * <p/>
    * @param reader The source to stream from
    */
   public StructuredTagStream2(StructureReader reader) {
      super(reader);
   }

   /**
    * Initializes a Structure for writing
    * <p/>
    * @param writer The destination to Stream to
    */
   public StructuredTagStream2(StructureWriter writer) {
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
         ((Field) last).endrecord = "";
         ((Field) last).endrecordregex = regexFromTag("");
      }
      super.add(e);
   }

   public abstract class Field extends StructuredStream.Field {

      public String startrecord;
      public String endrecord;
      public String startfield;
      public String endfield;
      public ByteRegex startrecordregex;
      public ByteRegex endrecordregex;
      public ByteRegex startfieldregex;
      public ByteRegex endfieldregex;

      public Field(FieldType type, String... label) {
         super(type, label);
         startfield = createStartFieldTag(this);
         endfield = createEndFieldTag(this);
         startrecord = (start.next == this) ? createStartRecord(this) : "";
         endrecord = createEndRecord(this);

         startfieldregex = createStartFieldRegex(this);
         endfieldregex = createEndFieldRegex(this);
         startrecordregex = createStartRecordRegex(this);
         endrecordregex = createEndRecordRegex(this);
      }

      public abstract boolean isArray();

      public boolean evaluateReadEnd() {
         return true;
      }

      public void setStartRecord(String tag) {
         this.startrecord = tag;
         this.startrecordregex = regexFromTag(tag);
      }

      public void setEndRecord(String tag) {
         this.endrecord = tag;
         this.endrecordregex = regexFromTag(tag);
      }

      public void setStartField(String tag) {
         this.startfield = tag;
         this.startfieldregex = regexFromTag(tag);
      }

      public void setEndField(String tag) {
         this.endfield = tag;
         this.endfieldregex = regexFromTag(tag);
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

      String arrayopen;
      String arrayclose;
      ByteRegex arrayopenregex;
      ByteRegex arraycloseregex;
      ByteRegex arraycloseorrecordregex;

      public ArrayField(FieldType type, String... label) {
         super(type, label);
         arrayopen = createStartArrayTag(this);
         arrayclose = createEndArrayTag(this);
         arrayopenregex = createStartArrayRegex(this);
         arraycloseregex = createEndArrayRegex(this);
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

      public IntField(String label) {
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
         return ((StructuredTagStream2) d).addInt(label[0]);
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

      public DoubleField(String label) {
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
         return ((StructuredTagStream2) d).addDouble(label[0]);
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

      public ByteField(String label) {
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
         return ((StructuredTagStream2) d).addByte(label[0]);
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

      public StringField(String label) {
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
         return ((StructuredTagStream2) d).addString(label[0]);
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

      public StringArrayField(String... label) {
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
         return ((StructuredTagStream2) d).addStringArray(label);
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

      public LongArrayField(String... label) {
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
         return ((StructuredTagStream2) d).addLongArray(label);
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

      public IntArrayField(String... label) {
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
         return ((StructuredTagStream2) d).addIntArray(label);
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

      public DoubleArrayField(String... label) {
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
         return ((StructuredTagStream2) d).addIntArray(label);
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

      public LongField(String label) {
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
         return ((StructuredTagStream2) d).addLong(label[0]);
      }

      public void readNoReturn() throws EOFException {
         read();
      }
   }
}
