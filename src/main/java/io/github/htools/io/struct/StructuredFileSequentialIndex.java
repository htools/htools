package io.github.htools.io.struct;

import io.github.htools.lib.Log;

/**
 * Internal helper class for StructuredFileSequential
 * @author jer
 */
class StructuredFileSequentialIndex extends StructuredFileOffsetLength {

   public Log log = new Log(StructuredFileSequentialIndex.class);
   StructuredFileSequential array;

   public StructuredFileSequentialIndex(StructuredFileSequential array) {
      super(array.getDatafile().getSubFile(".index"));
      this.array = array;
   }

   protected StructuredFile getSource() {
      return array;
   }
}
