package io.github.htools.type;

import io.github.htools.lib.MathTools;
import java.util.Map;

public class Tuple3Comparable<R extends Comparable, S extends Comparable, T extends Comparable> 
       implements Comparable<Tuple3Comparable<R, S, T>>, Map.Entry<R,Tuple2<S, T>> {

   public final R value1;
   public final S value2;
   public final T value3;

   public Tuple3Comparable(R r, S s, T t) {
      value1 = r;
      value2 = s;
      value3 = t;
   }

   public int compareTo(Tuple3Comparable<R, S, T> o) {
      int c = value1.compareTo(o.value1);
      if (c == 0) {
          c = value2.compareTo(o.value2);
          if (c == 0)
              c = value3.compareTo(o.value3);
      }
      return c; 
   }

   @Override
   public int hashCode() {
      return MathTools.hashCode(value1.hashCode(), value2.hashCode(), value3.hashCode());
   }

   @Override
   public boolean equals(Object o) {
      return (o instanceof Tuple3Comparable && 
              ((Tuple3Comparable) o).value1.equals(value1) && 
              ((Tuple3Comparable) o).value2.equals(value2) && 
              ((Tuple3Comparable) o).value3.equals(value3));
   }
   
   @Override
   public String toString() {
       return new StringBuilder().append("(").append(value1).append(",").append(value2).append(",").append(value3).append(")").toString();
   }

    @Override
    public R getKey() {
        return value1;
    }

    @Override
    public Tuple2<S, T> getValue() {
        return new Tuple2(value2, value3);
    }

    @Override
    public Tuple2<S, T> setValue(Tuple2<S, T> value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
