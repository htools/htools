package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;

/**
 * Stores an array of variable size records, so that a record can be found
 * relatively fast using a relatively small {@link RecordJump} table, that is
 * kept in a separate file. On {@link #openRead()}, the {@link RecordJump} table
 * is read into memory for fast access.
 * <p/>
 * @author jeroen
 */
public abstract class RecordJumpMemArray extends RecordJumpArray {

   public static Log log = new Log(RecordJumpMemArray.class);

   public RecordJumpMemArray(Datafile df) {
      super(df);
   }

   @Override
   public void openRead() {
      loadMem();
      jumparray.openRead();
   }
}
