package io.github.repir.tools.io.buffer;

import io.github.repir.tools.io.struct.StructureWriter;

/**
 *
 * @author Jeroen Vuurens
 */
public interface BufferSerializableWrite {

   void write(StructureWriter writer);
   
}
