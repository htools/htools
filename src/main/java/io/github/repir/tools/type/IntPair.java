package io.github.repir.tools.type;
import io.github.repir.tools.type.Tuple2Comparable;
import io.github.repir.tools.lib.Log;

public class IntPair extends Tuple2Comparable<Integer, Integer> {
   public static Log log = new Log(IntPair.class);

   public IntPair( int a, int b) {
      super(a,b);
   }
}
