package io.github.htools.io.struct;

import io.github.htools.io.DataIn;
import io.github.htools.io.DataOut;

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
