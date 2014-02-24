package io.github.repir.tools.DataTypes;

public class Tuple4<R, S, T, U> {

   public final R value1;
   public final S value2;
   public final T value3;
   public final U value4;

   public Tuple4(R r, S s, T t, U u) {
      value1 = r;
      value2 = s;
      value3 = t;
      value4 = u;
   }
}
