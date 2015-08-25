package io.github.htools.io.struct;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;

/**
 * Processes text data, with the elements in a fixed ordered sequence. In contrast
 * to {@link StructuredTextFile}, the elements are not necessarily identified by
 * their context, but by their position. Therefore, all elements must receive a value.
 * This can be used to read and write records with a simple separator such as a 
 * comma or a space, and records separated by an end of line. 
 * <p>
 * @author jeroen
 */
public abstract class StructuredTextTSV extends StructuredTextCSV {

   public static Log log = new Log(StructuredTextTSV.class);

   public StructuredTextTSV(BufferReaderWriter reader) {
       // #2, #3 are regex to identify beginning and end of field for reading
       // #4, #5 are Strings added before and after field when writing
      super( reader, "", "\t|$", "", "\t");
   }

   public StructuredTextTSV(Datafile writer) {
      super( writer, "", "\t|$", "", "\t");
   }
}
