/*
 */
package io.github.repir.tools.Content;

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

   int readBytes(long offset, byte b[], int pos, int length);

   void mustMoveBack();

   void openRead();

   InputStream getInputStream();
}
