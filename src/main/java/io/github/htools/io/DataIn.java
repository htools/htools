/*
 */
package io.github.htools.io;

import io.github.htools.io.buffer.BufferReaderWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 *
 * @author jeroen
 */
public interface DataIn {

   void close();

   void fillBuffer(BufferReaderWriter buffer) throws EOCException;

   void setBuffer(BufferReaderWriter buffer);

   long getLength() throws IOException ;

   public byte[] readFully() throws EOCException, IOException;

   int readBytes(long offset, byte b[], int pos, int length);

   void mustMoveBack() throws IOException;

   void openRead() throws IOException;

   InputStream getInputStream();
   
   boolean isCompressed();
}
