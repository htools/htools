package io.github.repir.tools.io.struct;

import io.github.repir.tools.io.DataIn;
import io.github.repir.tools.io.DataOut;

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
