package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;

public class RecordSequentialIndex extends RecordOffsetLength {

   public Log log = new Log(RecordSequentialIndex.class);
   RecordSequentialArray array;

   public RecordSequentialIndex(RecordSequentialArray array) {
      super(array.datafile.getSubFile(".index"));
      this.array = array;
   }

   protected RecordBinary getSource() {
      return array;
   }
}
