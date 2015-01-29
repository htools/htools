package io.github.repir.tools.io.struct;

import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.struct.StructureData;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
import java.util.Map;

/**
 * Provides streamed data access by iterative read/write actions of records/rows
 * that consists of some defined structure. Upon initialization, the
 * implementing class should override {@link #initStructure()}, in which the
 * structure is defined adding fields that extend {@link Element} to the
 * structure. These fields must be used to read/write rows in the exact order
 * the fields were added to the structure. Structure protects data integrity by
 * checking if reading/writing is performed according to the structure
 * definition, and throws a fatal error otherwise.
 * <p/>
 * @author jeroen
 */
public abstract class StructuredDataStream extends StructuredStream {

   public Log log = new Log(StructuredDataStream.class);

   protected StructuredDataStream() {
      super();
   }

   /**
    * Initializes a Structure for reading/writing
    * <p/>
    * @param readerwriter The object to Stream the data from or to
    */
   public StructuredDataStream(StructureData readerwriter) {
      super(readerwriter);
   }

   /**
    * for debugging purposes
    * <p/>
    * @return a string containing the structure, with iteration values,
    * indicating what the nextField is in squared brackets.
    */
//   @Override
//   public String toString() {
//      StringBuilder sb = new StringBuilder();
//      sb.append("");
//      sb.append(" { ");
//      for (StructuredStream.Field e = start; e != null; e = e.next) {
//         if (e == nextField) {
//            sb.append("[ ");
//         }
//         sb.append(e.label[0]);
//         sb.append(":");
//         switch (e.type) {
//            case ITERSTART:
//               sb.append("ITERATE(times=");
//               sb.append(((StructuredDataStream.IterField) e).counter);
//               sb.append(") ");
//               break;
//            case LONGITERSTART:
//               sb.append("LONGITERATE(times=");
//               sb.append(((StructuredDataStream.LongIterField) e).counter);
//               sb.append(") ");
//               break;
//            default:
//               sb.append(e.type.toString());
//               sb.append(" ");
//         }
//         if (e == nextField) {
//            sb.append("] ");
//         }
//      }
//      sb.append("}");
//      return sb.toString();
//   }
   /**
    * adds an int Iterator. This should only be used combined with
    * {@link StructuredDataStream#addIterEnd(java.lang.String)}. The iterator
    * marks a sub-sequence of fields that are iterated n-times when
    * reading/writing the Stream. The iterator holds an int to memorize the
    * number of iterations. upon reading the {@link IterField} value, the
    * iterations can be implemented using a while-loop that queries
    * {@link IterField#hasMore()}.
    * <p/>
    * @param label unique name of the Field
    * @return the added IterField
    */
   public IterField addIter(String label) {
      return new IterField(label);
   }

   /**
    * marks the endAfter of an {@link IterField}.
    * <p/>
    * @param label the same label as been used to define the corresponding
    * {@link IterField}
    */
   public IterEndField addIterEnd(IterField f) {
      return new IterEndField(f);
   }

   /**
    * like {@link IterField} but with a Long value.
    * <p/>
    * @param label unique name of the Field
    * @return the added LongIterField
    */
   public LongIterField addLongIter(String label) {
      return new LongIterField(label);
   }

   /**
    * marks the endAfter of an {@link LongIterField}.
    * <p/>
    * @param label the same label as been used to define the corresponding
    * {@link LongIterField}
    */
   public LongIterEndField addLongIterEnd(LongIterField f) {
      return new LongIterEndField(f);
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
    * add a 2-byte int
    * <p/>
    * @param label unique name of the Field
    * @return the added Int2Field
    */
   public Int2Field addInt2(String label) {
      return new Int2Field(label);
   }

   /**
    * add a 3-byte int
    * <p/>
    * @param label unique name of the Field
    * @return the added Int3Field
    */
   public Int3Field addInt3(String label) {
      return new Int3Field(label);
   }

   /**
    * add a compressed int
    * <p/>
    * @param label unique name of the Field
    * @return the added CIntField
    */
   public CIntField addCInt(String label) {
      return new CIntField(label);
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
    * add a compressed Double
    * <p/>
    * @param label unique name of the Field
    * @return the added CDoubleField
    */
   public CDoubleField addCDouble(String label) {
      return new CDoubleField(label);
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
    * add a String that is stored as a zero-terminated string.
    * <p/>
    * @param label unique name of the Field
    * @return the added String0Field
    */
   public String0Field addString0(String label) {
      return new String0Field(label);
   }

   /**
    * add an array of Strings
    * <p/>
    * @param label unique name of the Field
    * @return the added StringArrayField
    */
   public StringArrayField addStringArray(String label) {
      return new StringArrayField(label);
   }

   /**
    * add a Map<String,String>
    * <p/>
    * @param label unique name of the Field
    * @return the added StringPairField
    */
   public StringPairField addStringPairMap(String label) {
      return new StringPairField(label);
   }

   /**
    * add an array of ints
    * <p/>
    * @param label unique name of the Field
    * @return the added IntArrayField
    */
   public IntArrayField addIntArray(String label) {
      return new IntArrayField(label);
   }

   /**
    * add a 2-dimensional int array
    * <p/>
    * @param label unique name of the Field
    * @return the added IntArray2Field
    */
   public CIntArray2Field addCIntArray2(String label) {
      return new CIntArray2Field(label);
   }

   /**
    * add a 2-dimensional int array which is stored compressed
    * <p/>
    * @param label unique name of the Field
    * @return the added CIntArray2Field
    */
   public SquaredIntArray2Field addSquaredIntArray2(String label) {
      return new SquaredIntArray2Field(label);
   }

   /**
    * add a 2-dimensional int array, which is stored as a sparse array
    * <p/>
    * @param label unique name of the Field
    * @return the added Field
    */
   public IntSparse2Field addIntSparse2(String label) {
      return new IntSparse2Field(label);
   }

   /**
    * add a 3-dimensional int array
    * <p/>
    * @param label unique name of the Field
    * @return the added IntArray3Field
    */
   public CIntArray3Field addCIntArray3(String label) {
      return new CIntArray3Field(label);
   }

   /**
    * add a 3-dimensional int array, which is stored compressed
    * <p/>
    * @param label unique name of the Field
    * @return the added CIntArray3Field
    */
   public SquaredIntArray3Field addSquaredIntArray3(String label) {
      return new SquaredIntArray3Field(label);
   }

   /**
    * add a 3-dimensional int array, which is stored as a sparse array
    * <p/>
    * @param label unique name of the Field
    * @return the added IntSparse3Field
    */
   public IntSparse3Field addIntSparse3(String label) {
      return new IntSparse3Field(label);
   }

   /**
    * add an array of doubles, which is stored as a sparse array
    * <p/>
    * @param label unique name of the Field
    * @return the added DoubleSparseField
    */
   public DoubleSparseField addDoubleSparse(String label) {
      return new DoubleSparseField(label);
   }

   /**
    * add an array of integer, which is stored compressed
    * <p/>
    * @param label unique name of the Field
    * @return the added CIntArrayField
    */
   public CIntArrayField addCIntArray(String label) {
      return new CIntArrayField(label);
   }

   /**
    * add an array of sorted ints, which are stored as compressed incremental
    * ints
    * <p/>
    * @param label unique name of the Field
    * @return the added CIntIncrField
    */
   public CIntIncrField addCIntIncr(String label) {
      return new CIntIncrField(label);
   }

   /**
    * add an array of Longs
    * <p/>
    * @param label unique name of the Field
    * @return the added LongArrayField
    */
   public LongArrayField addLongArray(String label) {
      return new LongArrayField(label);
   }

   /**
    * add an array of Doubles
    * <p/>
    * @param label unique name of the Field
    * @return the added LongArrayField
    */
   public DoubleArrayField addDoubleArray(String label) {
      return new DoubleArrayField(label);
   }

   /**
    * add a 2-dimensional array of Longs.
    * <p/>
    * @param label unique name of the Field
    * @return the added LongArray2Field
    */
   public LongCArray2Field addSquaredLongArray2(String label) {
      return new LongCArray2Field(label);
   }

   /**
    * add a 2-dimensional array of Longs, which is stored compressed as a sparse
    * array
    * <p/>
    * @param label unique name of the Field
    * @return the added LongSparse2Field
    */
   public LongSparse2Field addLongSparse2(String label) {
      return new LongSparse2Field(label);
   }

   /**
    * add an array of Longs, which is stored compressed
    * <p/>
    * @param label unique name of the Field
    * @return the added CLongArrayField
    */
   public CLongArrayField addCLongArray(String label) {
      return new CLongArrayField(label);
   }

   /**
    * add am array of Longs, which is stored compressed as a sparse array.
    * <p/>
    * @param label unique name of the Field
    * @return the added LongSparseField
    */
   public LongSparseField addLongSparse(String label) {
      return new LongSparseField(label);
   }

   /**
    * add an int array, which is stored as a compressed sparse array
    * <p/>
    * @param label unique name of the Field
    * @return the added IntSparseField
    */
   public IntSparseField addIntSparse(String label) {
      return new IntSparseField(label);
   }

   protected StartField addStart() {
      return new StartField();
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
    * add a boolean
    * <p/>
    * @param label unique name of the Field
    * @return the added ByteField
    */
   public BoolField addBoolean(String label) {
      return new BoolField(label);
   }

   /**
    * add a fixed length byte array
    * <p/>
    * @param label unique name of the Field
    * @return the added FixedMemField
    */
   public FixedMemField addFixedMem(String label, int length) {
      return new FixedMemField(label, length);
   }

   /**
    * add a byte array
    * <p/>
    * @param label unique name of the Field
    * @return the added FixedMemField
    */
   public MemField addMem(String label) {
      return new MemField(label);
   }

   /**
    * add a unsigned 1-byte integer
    * <p/>
    * @param label unique name of the Field
    * @return the added UnsignedByteField
    */
   public UnsignedByteField addUnsignedByte(String label) {
      return new UnsignedByteField(label);
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

   /**
    * add a Long, which is stored compressed
    * <p/>
    * @param label unique name of the Field
    * @return the added CLongField
    */
   public CLongField addCLong(String label) {
      return new CLongField(label);
   }

   /**
    * A Field that holds a 4-byte integer.
    */
   public class IntField extends Field {

      public int value;

      protected IntField(String label) {
         super(FieldType.INT, label);
      }

      public int read() throws EOCException {
         checkRead(this);
         return value = reader.readInt();
      }

      public void write(int i) {
         checkWrite(this);
         value = i;
         if (writer != null) {
            writer.write(i);
         }
         writeDone(this);
      }
      
      public int get() {
          return value;
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public void skip() throws EOCException {
         checkRead(this);
         reader.skip(4);
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addInt(label);
      }
   }

   /**
    * A Field that holds a 4-byte integer.
    */
   public class BoolField extends Field {

      public boolean value;

      protected BoolField(String label) {
         super(FieldType.BOOLEAN, label);
      }

      public boolean read() throws EOCException {
         checkRead(this);
         return value = reader.readBoolean();
      }

      public void write(boolean i) {
         checkWrite(this);
         value = i;
         if (writer != null) {
            writer.write(i);
         }
         writeDone(this);
      }

      public boolean get() {
          return value;
      }
      
      public void readNoReturn() throws EOCException {
         read();
      }

      public void skip() throws EOCException {
         checkRead(this);
         reader.skip(1);
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addBoolean(label);
      }
   }

   /**
    * a field that holds a 2-byte integer
    */
   public class Int2Field extends Field {

      public int value;

      protected Int2Field(String label) {
         super(FieldType.INT2, label);
      }

      public int read() throws EOCException {
         checkRead(this);
         return value = reader.readInt2();
      }

      public void write(int i) {
         checkWrite(this);
         value = i;
         if (writer != null) {
            writer.write2(i);
         }
         writeDone(this);
      }

      public int get() {
          return value;
      }
      
      public void readNoReturn() throws EOCException {
         read();
      }

      public void skip() throws EOCException {
         checkRead(this);
         reader.skip(2);
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addInt2(label);
      }
   }

   /**
    * a field that holds a 3-byte integer
    */
   public class Int3Field extends Field {

      public int value;

      protected Int3Field(String label) {
         super(FieldType.INT3, label);
      }

      public int read() throws EOCException {
         checkRead(this);
         return value = reader.readInt3();
      }

      public void write(int i) {
         checkWrite(this);
         value = i;
         if (writer != null) {
            writer.write3(i);
         }
         writeDone(this);
      }

      public int get() {
          return value;
      }
      
      public void readNoReturn() throws EOCException {
         read();
      }

      public void skip() throws EOCException {
         checkRead(this);
         reader.skip(3);
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addInt3(label);
      }
   }

   /**
    * A field that stores and reads an Integer compressed as 1-5 bytes. Although
    * StructuredSteam does not contain the implementation of the compression
    * used, commonly, this is beneficial if small int values are more likely to
    * occur than large ones.
    */
   public class CIntField extends Field {

      public int value;

      protected CIntField(String label) {
         super(FieldType.CINT, label);
      }

      public int read() throws EOCException {
         checkRead(this);
         return value = reader.readCInt();
      }

      public void write(int i) {
         checkWrite(this);
         value = i;
         if (writer != null) {
            writer.writeC(i);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipCInt();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addCInt(label);
      }
   }

   /**
    * A Field that contains one Double value.
    */
   public class DoubleField extends Field {

      public double value;

      protected DoubleField(String label) {
         super(FieldType.DOUBLE, label);
      }

      public double read() throws EOCException {
         checkRead(this);
         return value = reader.readDouble();
      }

      public void write(double d) {
         checkWrite(this);
         value = d;
         if (writer != null) {
            writer.write(d);
         }
         writeDone(this);
      }

      public double get() {
          return value;
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public void skip() throws EOCException {
         checkRead(this);
         reader.skipDouble();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addDouble(label);
      }
   }

   /**
    * A Field that contains one Double value, which s stored compressed.
    */
   public class CDoubleField extends Field {

      public double value;

      protected CDoubleField(String label) {
         super(FieldType.CDOUBLE, label);
      }

      public double read() throws EOCException {
         checkRead(this);
         return value = reader.readCDouble();
      }

      public void write(double d) {
         checkWrite(this);
         value = d;
         if (writer != null) {
            writer.writeC(d);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public double get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipCDouble();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addCDouble(label);
      }
   }

   /**
    * A field that contains a signed 1-byte integer
    */
   public class ByteField extends Field {

      public int value;

      protected ByteField(String label) {
         super(FieldType.BYTE, label);
      }

      public int read() throws EOCException {
         checkRead(this);
         return value = reader.readByte();
      }

      public void write(byte b) {
         checkWrite(this);
         value = b;
         if (writer != null) {
            writer.write(b);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipByte();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addByte(label);
      }
   }

   /**
    * A Field that contains a fixed size byte array.
    */
   public class FixedMemField extends Field {

      public byte value[];
      private int length;

      protected FixedMemField(String label, int length) {
         super(FieldType.FIXEDMEM, label);
         this.length = length;
      }

      public byte[] read() throws EOCException {
         checkRead(this);
         return value = reader.readBytes(length);
      }

      public void write(byte b[]) {
         checkWrite(this);
         value = b;
         if (writer != null) {
            writer.writeRaw(b);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public byte[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skip(length);
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addFixedMem(label, length);
      }
   }

   /**
    * A Field that contains a fixed size byte array.
    */
   public class MemField extends Field {

      public byte value[];

      protected MemField(String label) {
         super(FieldType.MEM, label);
      }

      public byte[] read() throws EOCException {
         checkRead(this);
         return value = reader.readByteArray();
      }

      public void write(byte b[]) {
         checkWrite(this);
         if (writer != null) {
            writer.write(value = b);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public byte[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipByteBlock();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addMem(label);
      }
   }

   /**
    * a Field that contains an unsigned 1-byte integer
    */
   public class UnsignedByteField extends Field {

      public int value;

      protected UnsignedByteField(String label) {
         super(FieldType.UNSIGNEDBYTE, label);
      }

      public int read() throws EOCException {
         checkRead(this);
         return value = reader.readByte();
      }

      public void write(int b) {
         checkWrite(this);
         value = b;
         if (writer != null) {
            writer.writeUB(b);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipByte();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addUnsignedByte(label);
      }
   }

   /**
    * a Field that holds one String. Although the storage is implemented through
    * {@link StructureReader} and {@link StructureWriter}, the intention is that
    * the String is stored with an Integer that contains the string length
    * preceding the string. This enables fast skipping through Strings, although
    * this may not work when the String contains Unicode characters.
    */
   public class StringField extends Field {

      public String value;

      protected StringField(String label) {
         super(FieldType.STRING, label);
      }

      public String read() throws EOCException {
         checkRead(this);
         return value = reader.readString();
      }

      public void write(String s) {
         checkWrite(this);
         value = s;
         if (writer != null) {
            writer.write(s);
         }
         writeDone(this);
      }

      public void write(byte b[]) {
         checkWrite(this);
         if (writer != null) {
            writer.write(b);
         }
         writeDone(this);
      }

      @Override
      public void readNoReturn() throws EOCException {
         read();
      }

      public String get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipString();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addString(label);
      }
   }

   /**
    * a field that contains a String, which is stored as a 0-terminated sequence
    * of bytes.
    */
   public class String0Field extends Field {

      public String value;

      protected String0Field(String label) {
         super(FieldType.STRING0, label);
      }

      public String read() throws EOCException {
         checkRead(this);
         return value = reader.readString0();
      }

      public void write(String s) {
         checkWrite(this);
         value = s;
         if (writer != null) {
            writer.write0(s);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public String get() {
          return value;
      }

      public void skip() throws EOCException {
         checkRead(this);
         reader.skipString0();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addString0(label);
      }
   }

   /**
    * field that contains an ArrayTools of Strings. Usually, an integer that
    * contains the array size precedes the string data.
    */
   public class StringArrayField extends Field {

      public String[] value;

      protected StringArrayField(String label) {
         super(FieldType.STRINGARRAY, label);
      }

      public String[] read() throws EOCException {
         checkRead(this);
         return value = reader.readStringArray();
      }

      public void write(String array[]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.write(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public String[] get() {
          return value;
      }

      public void skip() throws EOCException {
         checkRead(this);
         reader.skipStringArray();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addStringArray(label);
      }
   }

   /**
    * a field that contains a map of String keys and String values.
    */
   public class StringPairField extends Field {

      public Map<String, String> value;

      protected StringPairField(String label) {
         super(FieldType.STRINGPAIRARRAY, label);
      }

      public Map<String, String> read() throws EOCException {
         checkRead(this);
         return value = reader.readStringPairMap();
      }

      public void write(Map<String, String> map) {
         checkWrite(this);
         value = map;
         if (writer != null) {
            writer.write(map);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public Map<String, String> get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipStringPairMap();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addStringPairMap(label);
      }
   }

   /**
    * a field that contains an array of Longs.
    */
   public class LongArrayField extends Field {

      public long[] value;

      protected LongArrayField(String label) {
         super(FieldType.LONGARRAY, label);
      }

      public long[] read() throws EOCException {
         checkRead(this);
         return value = reader.readLongArray();
      }

      public void write(long array[]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.write(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public long[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipLongArray();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addLongArray(label);
      }
   }

   /**
    * a field that contains an array of Doubles.
    */
   public class DoubleArrayField extends Field {

      public double[] value;

      protected DoubleArrayField(String label) {
         super(FieldType.DOUBLEARRAY, label);
      }

      public double[] read() throws EOCException {
         checkRead(this);
         return value = reader.readDoubleArray();
      }

      public void write(double array[]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.write(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public double[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipLongArray();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addLongArray(label);
      }
   }

   /**
    * a field that contains a 2-dimensional array of Longs.
    */
   public class LongCArray2Field extends Field {

      public long[][] value;

      protected LongCArray2Field(String label) {
         super(FieldType.CLONGARRAY2, label);
      }

      public long[][] read() throws EOCException {
         checkRead(this);
         return value = reader.readCLongArray2();
      }

      public void write(long array[][]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeC(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public long[][] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipCLongArray2();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addSquaredLongArray2(label);
      }
   }

   /**
    * a field that contains a sparse 2-dimensional array of Longs. Choose this
    * type for arrays that contain many 0-values, saves space in storage.
    */
   public class LongSparse2Field extends Field {

      public long[][] value;

      protected LongSparse2Field(String label) {
         super(FieldType.LONGSPARSE2, label);
      }

      public long[][] read() throws EOCException {
         checkRead(this);
         return value = reader.readLongSparse2();
      }

      public void write(long array[][]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeSparse(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public long[][] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipLongSparse2();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addLongSparse2(label);
      }
   }

   /**
    * a field that contains an int array.
    */
   public class IntArrayField extends Field {

      public int[] value;
      public ArrayList<Integer> alvalue;

      protected IntArrayField(String label) {
         super(FieldType.INTARRAY, label);
      }

      public int[] read() throws EOCException {
         checkRead(this);
         return value = reader.readIntArray();
      }

      public void write(int array[]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.write(array);
         }
         writeDone(this);
      }

      public void write(ArrayList<Integer> list) {
         checkRead(this);
         alvalue = list;
         if (writer != null) {
            writer.write(list);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipIntArray();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addIntArray(label);
      }
   }

   /**
    * a field that contains a 2-dimensional in array, that is stored compressed.
    * This FieldType saves storage space if small int values are more common
    * than large ones.
    */
   public class SquaredIntArray2Field extends Field {

      public int[][] value;

      protected SquaredIntArray2Field(String label) {
         super(FieldType.SQUAREDINTARRAY2, label);
      }

      public int[][] read() throws EOCException {
         checkRead(this);
         return value = reader.readSquaredIntArray2();
      }

      public void write(int array[][]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeSquared(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[][] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipSquaredIntArray2();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addSquaredIntArray2(label);
      }
   }

   /**
    * a field that contains a 2-dimensional int array.
    */
   public class CIntArray2Field extends Field {

      public int[][] value;

      protected CIntArray2Field(String label) {
         super(FieldType.CINTARRAY2, label);
      }

      public int[][] read() throws EOCException {
         checkRead(this);
         return value = reader.readCIntArray2();
      }

      public void write(int array[][]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeC(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[][] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipCIntArray2();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addCIntArray2(label);
      }
   }

   /**
    * a field that contains a 2-dimensional int array. This FieldType saves
    * storage space if the data contain many 0-values.
    */
   public class IntSparse2Field extends Field {

      public int[][] value;

      protected IntSparse2Field(String label) {
         super(FieldType.INTSPARSE2, label);
      }

      public int[][] read() throws EOCException {
         checkRead(this);
         return value = reader.readIntSparse2();
      }

      public void write(int array[][]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeSparse(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[][] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipIntSparse2();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addIntSparse2(label);
      }
   }

   /**
    * a field that contains a 3-dimensional int array, which is stored
    * compressed. this FieldType saves space is small int-values are more common
    * than large ones.
    */
   public class SquaredIntArray3Field extends Field {

      public int[][][] value;

      protected SquaredIntArray3Field(String label) {
         super(FieldType.SQUAREDINTARRAY3, label);
      }

      public int[][][] read() throws EOCException {
         checkRead(this);
         return value = reader.readSquaredIntArray3();
      }

      public void write(int array[][][]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeSquared(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[][][] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipSquaredIntArray3();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addSquaredIntArray3(label);
      }
   }

   /**
    * a field that contains a 3-dimensional int array.
    */
   public class CIntArray3Field extends Field {

      public int[][][] value;

      protected CIntArray3Field(String label) {
         super(FieldType.CINTARRAY3, label);
      }

      public int[][][] read() throws EOCException {
         checkRead(this);
         return value = reader.readCIntArray3();
      }

      public void write(int array[][][]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeC(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[][][] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipCIntArray3();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addCIntArray3(label);
      }
   }

   /**
    * a field that contains a 3-dimensional int array. This FieldType saves
    * storage space if the array contains many 0-values.
    */
   public class IntSparse3Field extends Field {

      public int[][][] value;

      protected IntSparse3Field(String label) {
         super(FieldType.INTSPARSE3, label);
      }

      public int[][][] read() throws EOCException {
         checkRead(this);
         return value = reader.readIntSparse3();
      }

      public void write(int array[][][]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeSparse(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[][][] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipIntSparse3();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addIntSparse3(label);
      }
   }

   /**
    * a field that contains an int array. This FieldType saves storage space if
    * small int-values are more common than large ones.
    */
   public class CIntArrayField extends Field {

      public int[] value;
      public ArrayList<Integer> alvalue;

      protected CIntArrayField(String label) {
         super(FieldType.CINTARRAY, label);
      }

      public int[] read() throws EOCException {
         checkRead(this);
         return value = reader.readCIntArray();
      }

      public ArrayList<Integer> readArrayList() throws EOCException {
         checkRead(this);
         return alvalue = reader.readCIntArrayList();
      }

      public void write(int array[]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeC(array);
            writeDone(this);
         } else {
            log.fatal("writer is null");
         }
      }

      public void write(ArrayList<Integer> array) {
         checkWrite(this);
         alvalue = array;
         if (writer != null) {
            writer.writeC(array);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[] get() {
          return value;
      }
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipCIntArray();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addCIntArray(label);
      }
   }

   /**
    * a Field that contains an int array. You should only use this type if the
    * values in the array are sorted small to large.
    */
   public class CIntIncrField extends Field {

      public int[] value;
      public ArrayList<Integer> alvalue;

      protected CIntIncrField(String label) {
         super(FieldType.CINTINCR, label);
      }

      public int[] read() throws EOCException {
         checkRead(this);
         return value = reader.readCIntIncr();
      }

      /* warning destructs array */
      public void write(int array[]) {
         checkWrite(this);
         value = array;
         if (writer != null) {
            writer.writeIncr(array);
         }
         writeDone(this);
      }

      public void write(ArrayList<Integer> al) {
         write(io.github.repir.tools.lib.ArrayTools.toIntArray(al));
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipCIntArray();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addCIntIncr(label);
      }
   }

   /**
    * a field that contains a Long.
    */
   public class LongField extends Field {

      public long value;

      protected LongField(String label) {
         super(FieldType.LONG, label);
      }

      public long read() throws EOCException {
         checkRead(this);
         return value = reader.readLong();
      }

      public void write(long l) {
         checkWrite(this);
         value = l;
         if (writer != null) {
            writer.write(l);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public long get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipLong();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addLong(label);
      }
   }

   /**
    * a field that contains a Long, that is stored compressed, using 1-9 bytes.
    * This FieldType should be preferred if smaller values occur more frequently
    * than large values.
    */
   public class CLongField extends Field {

      public long value;

      protected CLongField(String label) {
         super(FieldType.CLONG, label);
      }

      public long read() throws EOCException {
         checkRead(this);
         return value = reader.readCLong();
      }

      public void write(long l) {
         checkWrite(this);
         value = l;
         if (writer != null) {
            writer.writeC(l);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public long get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipCLong();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addCLong(label);
      }
   }

   /**
    * a field that contains an array of Longs, which are stored compressed. Use
    * this type if small Long values occur more frequently than large ones.
    */
   public class CLongArrayField extends Field {

      public long[] value;

      protected CLongArrayField(String label) {
         super(FieldType.CLONGARRAY, label);
      }

      public long[] read() throws EOCException {
         checkRead(this);
         return value = reader.readCLongArray();
      }

      public void write(long l[]) {
         checkWrite(this);
         value = l;
         if (writer != null) {
            writer.writeC(l);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public long[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipCLongArray();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addCLongArray(label);
      }
   }

   /**
    * a field that contains an array of Longs. This FieldType saves storage
    * space when the array contains many 0-values.
    */
   public class LongSparseField extends Field {

      public long[] value;

      protected LongSparseField(String label) {
         super(FieldType.LONGSPARSE, label);
      }

      public long[] read() throws EOCException {
         checkRead(this);
         return value = reader.readLongSparse();
      }

      public Map<Integer, Long> readMap() throws EOCException {
         checkRead(this);
         return reader.readSparseLongMap();
      }

      public void write(long l[]) {
         checkWrite(this);
         value = l;
         if (writer != null) {
            writer.writeSparse(l);
         }
         writeDone(this);
      }

      public void write(Map<Integer, Long> l) {
         checkWrite(this);
         if (writer != null) {
            writer.writeSparseLong(l);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public long[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipLongSparse();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addLongSparse(label);
      }
   }

   /**
    * a field that contains an array of integers. This FieldType saves storage
    * space if the array contains many 0-values.
    */
   public class IntSparseField extends Field {

      public int[] value;

      protected IntSparseField(String label) {
         super(FieldType.INTSPARSE, label);
      }

      public int[] read() throws EOCException {
         checkRead(this);
         return value = reader.readIntSparse();
      }

      public Map<Integer, Integer> readMap() throws EOCException {
         checkRead(this);
         return reader.readSparseIntMap();
      }

      public void write(int l[]) {
         if (writer != null) {
            checkWrite(this);
            value = l;
            writer.writeSparse(l);
            writeDone(this);
         } else {
            log.fatal("writer is null");
         }
      }

      public void write(Map<Integer, Integer> l) {
         checkWrite(this);
         if (writer != null) {
            writer.writeSparseInt(l);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public int[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipIntSparse();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addIntSparse(label);
      }
   }

   /**
    * a field that contains an array of doubles. This FieldType saves storage
    * space if the array contains many 0-values.
    */
   public class DoubleSparseField extends Field {

      public double[] value;

      protected DoubleSparseField(String label) {
         super(FieldType.DOUBLESPARSE, label);
      }

      public double[] read() throws EOCException {
         checkRead(this);
         return value = reader.readDoubleSparse();
      }

      public void write(double l[]) {
         checkWrite(this);
         value = l;
         if (writer != null) {
            writer.writeSparse(l);
         }
         writeDone(this);
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public double[] get() {
          return value;
      }
      
      public void skip() throws EOCException {
         checkRead(this);
         reader.skipDoubleSparse();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addDoubleSparse(label);
      }
   }

   /**
    * a field that enables to include multiple sub-records within a record.
    * IterField is written as an integer, that defines the number of iterations.
    * All fields in between the IterField and IterEndField are iteratively
    * read/written that many times. {@link IterField#hasMore()} can be used to
    * iterate through the sub-records.
    */
   public class IterField extends Field {

      public int counter = 0, value;
      IterEndField loop;

      protected IterField(String label) {
         super(FieldType.ITERSTART, label);
      }

      public boolean hasMore() {
         return counter > 0;
      }

      public int read() throws EOCException {
         checkRead(this);
         value = counter = reader.readInt();
         if (this.counter == 0) {
            nextField = loop.arrivingAt();
         } else {
            nextField = next.arrivingAt();
         }
         posMoved();
         return value;
      }

      public void write(int iter) {
         checkWrite(this);
         if (writer != null) {
            writer.write(counter = value = iter);
         }
         writeDone(this);
         if (iter == 0) {
            nextField = loop.arrivingAt();
         } else {
            nextField = next.arrivingAt();
         }
         posMoved();
      }

      @Override
      public Field nextField() { // this class moves to next in read() and write()
         return this;
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public void skip() throws EOCException {
         checkRead(this);
         read();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addIter(label);
      }
   }

   /**
    * like {@link IterField#hasMore()}, but with a Long instead of an Int.
    */
   public class LongIterField extends Field {

      public long value, counter = 0;
      LongIterEndField loop;

      protected LongIterField(String label) {
         super(FieldType.LONGITERSTART, label);
      }

      public boolean hasMore() {
         return counter > 0;
      }

      public long read() throws EOCException {
         checkRead(this);
         value = counter = reader.readLong();
         if (this.counter == 0) {
            nextField = loop.arrivingAt();
         } else {
            nextField = next.arrivingAt();
         }
         posMoved();
         return value;
      }

      public void write(long iter) {
         checkWrite(this);
         if (writer != null) {
            writer.write(iter);
         }
         writeDone(this);
         value = counter = iter;
         if (iter == 0) {
            nextField = loop.arrivingAt();
         } else {
            nextField = next.arrivingAt();
         }
         posMoved();
      }

      @Override
      public Field nextField() { // this class moves to next in read() and write()
         return this;
      }

      public void readNoReturn() throws EOCException {
         read();
      }

      public void skip() throws EOCException {
         read();
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addLongIter(label);
      }
   }

   protected class IterEndField extends Field {

      IterField loop;

      protected IterEndField(IterField f) {
         super(FieldType.ITEREND, f.label);
         f.loop = this;
         loop = f;
      }

      @Override
      public StructuredStream.Field arrivingAt() {
         if (--loop.counter > 0) {
            return loop.next.arrivingAt();
         }
         return nextField();
      }

      public void readNoReturn() {
      }

      public void skip() {
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addIterEnd((IterField) d.find(FieldType.ITEREND, label));
      }
   }

   protected class LongIterEndField extends Field {

      LongIterField loop;

      protected LongIterEndField(LongIterField f) {
         super(FieldType.LONGITEREND, f.label);
         f.loop = this;
         loop = f;
      }

      @Override
      public StructuredStream.Field arrivingAt() {
         if (--loop.counter > 0) {
            return loop.next.arrivingAt();
         }
         return nextField();
      }

      public void readNoReturn() {
      }

      public void skip() {
      }

      public Field clone(StructuredStream d) {
         return ((StructuredDataStream) d).addLongIterEnd((LongIterField) d.find(FieldType.LONGITERSTART, label));
      }
   }
}
