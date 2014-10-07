package io.github.repir.tools.Type;
import io.github.repir.tools.Type.Tuple2Comparable;
import io.github.repir.tools.Lib.Log;

public class IntPair extends Tuple2Comparable<Integer, Integer> {
   public static Log log = new Log(IntPair.class);

   public IntPair( int a, int b) {
      super(a,b);
   }
}
