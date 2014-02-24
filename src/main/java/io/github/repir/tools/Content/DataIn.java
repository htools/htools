/*
 */
package io.github.repir.tools.Content;

import java.io.EOFException;
import java.io.InputStream;
import java.util.HashMap;

/**
 *
 * @author jeroen
 */
public interface DataIn {

   void close();

   void fillBuffer(BufferReaderWriter buffer) throws EOFException;

   void setBuffer(BufferReaderWriter buffer);

   long getLength();

   int readBytes(long offset, byte b[], int pos, int length);

   void mustMoveBack();

   void openRead();

   InputStream getInputStream();
}
