package io.github.repir.tools.Buffer;

import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Structure.StructureReader;

/**
 *
 * @author Jeroen Vuurens
 */
public interface BufferSerializableRead {

   void read(StructureReader reader) throws EOCException;
   
}
