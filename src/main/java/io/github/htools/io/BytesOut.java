package io.github.htools.io;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;
import java.io.OutputStream;
import java.util.ArrayList;

public class BytesOut implements DataOut {

   public static Log log = new Log(BytesOut.class);
   BufferReaderWriter buffer;
   public ArrayList< byte[]> content = new ArrayList< byte[]>();

   public BytesOut() {
   }

   public final void setBuffer(BufferReaderWriter buffer) {
      this.buffer = buffer;
   }

   public void close() {
      throw new UnsupportedOperationException("not possible to close fixed byte array");
   }

   public void flushBuffer(BufferReaderWriter buffer) {
      byte c[] = new byte[buffer.bufferpos];
      System.arraycopy(buffer.buffer, 0, c, 0, buffer.bufferpos);
      content.add(c);
      buffer.offset += buffer.bufferpos;
      buffer.bufferpos = 0;
   }

   public void flushFile() {
      flushBuffer(buffer);
   }

   public byte[] getContent() {
      if (content.size() == 0) {
         return new byte[0];
      }
      int size = 0;
      for (byte[] b : content) {
         size += b.length;
      }
      byte r[] = new byte[size];
      int pos = 0;
      for (byte[] b : content) {
         System.arraycopy(b, 0, r, pos, b.length);
         pos += b.length;
      }
      content.clear();
      return r;
   }

   public void openWrite() {
   }

   public void openAppend() {
   }

   public OutputStream getOutputStream() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
