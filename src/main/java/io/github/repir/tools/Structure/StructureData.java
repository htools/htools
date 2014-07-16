package io.github.repir.tools.Structure;

import io.github.repir.tools.Content.DataIn;
import io.github.repir.tools.Content.DataOut;

/**
 *
 * @author jbpvuurens
 */
public interface StructureData extends StructureReader, StructureWriter {

   public void openWrite();

   @Override
   public void closeRead();

   public void closeWrite();

   @Override
   public boolean hasMore();

   @Override
   public void setDataIn(DataIn in);

   public void setDataOut(DataOut out);
}
