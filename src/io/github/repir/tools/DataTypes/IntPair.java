package io.github.repir.tools.DataTypes;
import io.github.repir.tools.Lib.Log;

public class IntPair extends Tuple2<Integer, Integer> {
   public static Log log = new Log(IntPair.class);

   public IntPair( int a, int b) {
      super(a,b);
   }
}
