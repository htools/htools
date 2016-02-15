package io.github.htools.type;

import io.github.htools.lib.ByteTools;

import java.util.Map;

public class Tuple2Comparable<R extends Comparable, S extends Comparable> implements Comparable<Tuple2Comparable<R, S>>,Map.Entry<R, S> {

   public final R value1;
   public final S value2;

   public Tuple2Comparable(R r, S s) {
      value1 = r;
      value2 = s;
   }

   public int compareTo(Tuple2Comparable<R, S> o) {
      int c = value1.compareTo(o.value1);
      return (c != 0) ? c : value2.compareTo(o.value2);
   }

   @Override
   public int hashCode() {
      return 3 + value1.hashCode() * 29 + value2.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      return (o instanceof Tuple2Comparable && ((Tuple2Comparable) o).value1.equals(value1) && ((Tuple2Comparable) o).value2.equals(value2));
   }
   
   @Override
   public String toString() {
       return new StringBuilder().append("(").append(value1).append(",").append(value2).append(")").toString();
   }

    @Override
    public R getKey() {
        return value1;
    }

    @Override
    public S getValue() {
        return value2;
    }

    @Override
    public S setValue(S value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static Tuple2Comparable<Integer, Integer> find(byte[] haystack, byte[] needlestart, byte[] needleend, int startpos, int endpos, boolean ignorecase, boolean omitquotes) {
        int needlepos = ByteTools.find(haystack, needlestart, startpos, endpos, ignorecase, false);
        if (needlepos > -1) {
            int needlepos2 = ByteTools.find(haystack, needleend, needlepos + needlestart.length, endpos, ignorecase, omitquotes);
            if (needlepos2 > -1) {
                return new Tuple2Comparable<Integer, Integer>(needlepos, needlepos2);
            }
        }
        return null;
    }

}
