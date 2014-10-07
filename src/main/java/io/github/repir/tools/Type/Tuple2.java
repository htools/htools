package io.github.repir.tools.Type;

public class Tuple2<R, S> {

   public R value1;
   public S value2;

   public Tuple2(R r, S s) {
      value1 = r;
      value2 = s;
   }

   @Override
   public int hashCode() {
      return 3 + value1.hashCode() * 29 + value2.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      return (o instanceof Tuple2 && ((Tuple2) o).value1.equals(value1) && ((Tuple2) o).value2.equals(value2));
   }
}
