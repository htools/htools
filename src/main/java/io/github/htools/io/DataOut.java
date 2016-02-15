/*
 */
package io.github.htools.io;

import io.github.htools.io.buffer.BufferReaderWriter;

import java.io.OutputStream;

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
