package io.github.htools.hadoop.io.archivereader;

import io.github.htools.io.EOCException;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;
import org.apache.hadoop.io.BytesWritable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jeroen
 */
public class RecordValue extends BytesWritable {

   public static Log log = new Log(RecordValue.class);
   public BufferReaderWriter reader = new BufferReaderWriter();
   public BufferDelayedWriter writer = new BufferDelayedWriter();

   public RecordValue() {
   }

   @Override
   public void readFields(DataInput in) throws IOException {
      try {
         int length = in.readInt();
         byte b[] = new byte[length];
         in.readFully(b);
         reader.setBuffer(b);
      } catch (EOCException ex) {
         throw new IOException(ex);
      }
   }

   @Override
   public void write(DataOutput out) throws IOException {
      byte b[] = writer.getAsByteBlock();
      out.write(b);
   }
}
