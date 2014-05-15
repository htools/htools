package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;

/**
 * Internal helper class for StructuredFileSequential
 * @author jer
 */
class StructuredFileSequentialIndex extends StructuredFileOffsetLength {

   public Log log = new Log(StructuredFileSequentialIndex.class);
   StructuredFileSequential array;

   public StructuredFileSequentialIndex(StructuredFileSequential array) {
      super(array.datafile.getSubFile(".index"));
      this.array = array;
   }

   protected StructuredFile getSource() {
      return array;
   }
}
