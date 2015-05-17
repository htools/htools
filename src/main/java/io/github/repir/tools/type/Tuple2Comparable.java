package io.github.repir.tools.type;

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
}
