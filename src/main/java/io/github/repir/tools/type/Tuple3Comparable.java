package io.github.repir.tools.type;

import io.github.repir.tools.lib.MathTools;

public class Tuple3Comparable<R extends Comparable, S extends Comparable, T extends Comparable> implements Comparable<Tuple3Comparable<R, S, T>> {

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
}
