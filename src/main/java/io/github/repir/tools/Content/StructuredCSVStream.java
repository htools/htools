package io.github.repir.tools.Content;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

/**
 * Provides streamed data access by iterative read/write actions of records/rows
 * that consists of some defined structure. This class is intended to read/write
 * content to flat text files, so the data can be read/written/changed outside
 * the program.
 * <p/>
 * To facilitate writing data to flat files, a field separator is required. By
 * default the row separator is a newline and the field terminator is a space.
 * This means String containing these characters must be escaped by an escape
 * character which defaults to a backslash.
 * <p/>
 * This implementation has no whitespace tolerance like
 * {@link StructuredTagStream}.
 * <p/>
 * @author jeroen
 */
public abstract class StructuredCSVStream extends StructuredTagStream2 {

   public Log log = new Log(StructuredCSVStream.class);

   protected StructuredCSVStream() {
      super();
   }

   public StructuredCSVStream(StructureData readerwriter) {
      super(readerwriter);
   }

   public StructuredCSVStream(StructureReader reader) {
      super(reader);
   }

   public StructuredCSVStream(StructureWriter writer) {
      super(writer);
   }

   @Override
   public String createStartFieldTag(Field f) {
      return "";
   }

   @Override
   public String createStartRecord(Field f) {
      return "";
   }

   @Override
   public String createEndRecord(Field f) {
      return "\n";
   }

   @Override
   public ByteRegex createEndRecordRegex(Field f) {
      return new ByteRegex("\\n");
   }

   @Override
   public String createEndFieldTag(Field f) {
      return " ";
   }

   @Override
   public ByteRegex createEndFieldRegex(Field f) {
      return new ByteRegex(new ByteRegex(createEndFieldTag(f)), createEndRecordRegex(f).lookAhead());
   }

   @Override
   public String createStartArrayTag(ArrayField f) {
      return "";
   }

   @Override
   public String createEndArrayTag(ArrayField f) {
      return "";
   }

   @Override
   public ArrayList<String> readArray(ArrayField f) throws EOFException {
      startRead(f);
      ArrayList<String> al = new ArrayList<String>();
      skipString(f.arrayopenregex);
      while (peekStringNotExists(f.endrecordregex) && peekStringExists(f.startfieldregex)) {
         readString(f.startfieldregex);
         al.add(readString(f.endfieldregex));
      }
      skipString(f.arraycloseregex);
      endRead(f);
      return al;
   }

   public void writeArray(ArrayField f, String al[]) {
      startWrite(f);
      if (al.length > 0) {
         writer.write(f.arrayopen);
         for (String s : al) {
            byte content[] = s.getBytes();
            writer.write(f.startfield);
            writer.write(escapeString(s.getBytes(), f.endfieldregex, f.endrecordregex));
            writer.write(f.endfield);
         }
         writer.write(f.arrayclose);
      }
      endWrite(f);
   }
}