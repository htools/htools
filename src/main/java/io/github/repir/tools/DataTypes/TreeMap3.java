package io.github.repir.tools.DataTypes;

import io.github.repir.tools.Lib.Log;
import java.util.Comparator;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class TreeMap3<K extends Comparable, V1, V2> extends TreeMapComparable<K, Tuple2<V1, V2>> {

   public static Log log = new Log(TreeMap3.class);
   
   public TreeMap3( TYPE t ) {
      super(t);
   }
   
   public TreeMap3( TYPE t, Comparator c ) {
      super(t, c);
   }
   
   public Tuple2<V1, V2> put(K k, V1 v1, V2 v2) {
       return super.put(k, new Tuple2<V1, V2>(v1, v2));
   }
}
