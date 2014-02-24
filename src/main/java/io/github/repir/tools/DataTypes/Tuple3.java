package io.github.repir.tools.DataTypes;

public class Tuple3<R, S, T> {

   public final R value1;
   public final S value2;
   public final T value3;

   public Tuple3(R r, S s, T t) {
      value1 = r;
      value2 = s;
      value3 = t;
   }
}
