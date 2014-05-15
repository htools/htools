package io.github.repir.tools.Content;

/**
 *
 * @author Jeroen Vuurens
 */
public interface BufferSerializableRead {

   void read(StructureReader reader) throws EOCException;
   
}
