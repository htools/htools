/*
 */
package io.github.repir.tools.Content;

import java.io.OutputStream;
import java.util.Map;

/**
 *
 * @author jeroen
 */
public interface DataOut {

   void close();

   void flushBuffer(BufferReaderWriter buffer);

   void setBuffer(BufferReaderWriter buffer);

   void flushFile();

   void openWrite();

   void openAppend();

   OutputStream getOutputStream();
}
