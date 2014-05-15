package io.github.repir.tools.DataTypes;

public class Tuple2M<R extends Comparable, S extends Comparable> implements Comparable<Tuple2M<R, S>> {

   public R value1;
   public S value2;

   public Tuple2M() {;
   }

   public Tuple2M(R r, S s) {
      value1 = r;
      value2 = s;
   }

   public int compareTo(Tuple2M<R, S> o) {
      int c = value1.compareTo(o.value1);
      return (c != 0) ? c : value2.compareTo(o.value2);
   }

   @Override
   public int hashCode() {
      return 3 + value1.hashCode() * 29 + value2.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      return (o instanceof Tuple2M && ((Tuple2M) o).value1.equals(value1) && ((Tuple2M) o).value2.equals(value2));
   }
}
