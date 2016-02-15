package io.github.htools.io;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An in memory byte buffer that acts as OutputStream and implements DataOut
 * for use with Datafile, BufferReaderWriter and BufferDelayedWriter.
 */
public class BytesOut extends ByteArrayOutputStream implements DataOut {

   public static Log log = new Log(BytesOut.class);
   BufferReaderWriter buffer;

   public BytesOut() {
   }

   public final void setBuffer(BufferReaderWriter buffer) {
      this.buffer = buffer;
   }

   public void close() {
      try {
         super.close();
      } catch (IOException e) {
         log.fatal("fatal error closing BytesOut");
      }
   }

   public void flushBuffer(BufferReaderWriter buffer) {
      super.write(buffer.buffer, 0, buffer.bufferpos);
      buffer.bufferpos = 0;
   }

   public void flushFile() {
      flushBuffer(buffer);
   }

   public byte[] getContent() {
      byte[] result = super.toByteArray();
      super.reset();
      return result;
   }

   public void openWrite() {
   }

   public OutputStream getOutputStream() {
      return this;
   }
}
