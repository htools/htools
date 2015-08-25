package io.github.htools.io.buffer;

import io.github.htools.io.struct.StructureWriter;

/**
 *
 * @author Jeroen Vuurens
 */
public interface BufferSerializableWrite {

   void write(StructureWriter writer);
   
}
