package io.github.repir.tools.Type;

public class Tuple2Comparable<R extends Comparable, S extends Comparable> implements Comparable<Tuple2Comparable<R, S>> {

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
}
