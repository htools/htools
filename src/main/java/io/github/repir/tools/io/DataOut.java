/*
 */
package io.github.repir.tools.io;

import io.github.repir.tools.io.buffer.BufferReaderWriter;
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

   OutputStream getOutputStream();
}
