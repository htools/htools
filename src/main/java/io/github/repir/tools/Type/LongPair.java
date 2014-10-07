package io.github.repir.tools.Type;
import io.github.repir.tools.Type.Tuple2Comparable;
import io.github.repir.tools.Lib.Log;

public class LongPair extends Tuple2Comparable<Long, Long> {
   public static Log log = new Log(LongPair.class);

   public LongPair( long a, long b) {
      super(a,b);
   }
}
