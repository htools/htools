package io.github.htools.type;

import io.github.htools.lib.Log;

public class IntPair extends Tuple2Comparable<Integer, Integer> {
   public static Log log = new Log(IntPair.class);

   public IntPair( int a, int b) {
      super(a,b);
   }
}
