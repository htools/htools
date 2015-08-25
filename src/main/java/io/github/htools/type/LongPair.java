package io.github.htools.type;
import io.github.htools.type.Tuple2Comparable;
import io.github.htools.lib.Log;

public class LongPair extends Tuple2Comparable<Long, Long> {
   public static Log log = new Log(LongPair.class);

   public LongPair( long a, long b) {
      super(a,b);
   }
}
