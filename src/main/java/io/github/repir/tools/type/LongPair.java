package io.github.repir.tools.type;
import io.github.repir.tools.type.Tuple2Comparable;
import io.github.repir.tools.lib.Log;

public class LongPair extends Tuple2Comparable<Long, Long> {
   public static Log log = new Log(LongPair.class);

   public LongPair( long a, long b) {
      super(a,b);
   }
}
