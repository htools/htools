package io.github.repir.tools.collection;

import io.github.repir.tools.type.Tuple2;
import io.github.repir.tools.lib.Log;
import java.util.LinkedList;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class QueueMap<K, V> extends LinkedList<Tuple2<K, V>> {

   public static Log log = new Log(QueueMap.class);
   
   public QueueMap( ) {
      super();
   }
   
   public void add(K k, V v) {
       super.add( new Tuple2<K, V>(k, v));
   }
}
