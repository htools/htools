package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author jbpvuurens
 */
public interface StructureData extends StructureReader, StructureWriter {

   public void openWrite();

   public void closeRead();

   public void closeWrite();

   public boolean hasMore();

   @Override
   public void setDataIn(DataIn in);

   public void setDataOut(DataOut out);
}
