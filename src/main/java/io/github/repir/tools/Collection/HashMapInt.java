package io.github.repir.tools.Collection;

import io.github.repir.tools.Lib.Log;
import java.util.HashMap;
/**
 *
 * @author jeroen
 */
public class HashMapInt<K> extends HashMap<K, Integer> {
   public static final Log log = new Log( HashMapInt.class );

   public HashMapInt() {
       super();
   }
   
   public void add(K key, int value) {
       Integer oldvalue = get(key);
       put(key, oldvalue==null?value:oldvalue+value);
   }
   
}
