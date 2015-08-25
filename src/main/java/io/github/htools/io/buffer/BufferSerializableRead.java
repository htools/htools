package io.github.htools.io.buffer;

import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructureReader;

/**
 *
 * @author Jeroen Vuurens
 */
public interface BufferSerializableRead {

   void read(StructureReader reader) throws EOCException;
   
}
