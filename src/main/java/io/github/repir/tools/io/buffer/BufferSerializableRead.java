package io.github.repir.tools.io.buffer;

import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.struct.StructureReader;

/**
 *
 * @author Jeroen Vuurens
 */
public interface BufferSerializableRead {

   void read(StructureReader reader) throws EOCException;
   
}
