package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;

/**
 * A HashMap with a HashSet of values, that supports adding values to existing keys
 * @author jeroen
 */
public class FHashMap<K, V> extends 
        Object2ObjectOpenHashMap<K, V> {

   public static Log log = new Log(FHashMap.class);
   
   public FHashMap( ) {
      super();
   }
   
   public FHashMap( int size ) {
      super( size );
   }
   
   public FHashMap( Map<K, V> map ) {
      super(map);
   }
   
   public FHashMap( final int expected, Map<K, V> map ) {
      super(expected);
      putAll(map);
   }
   
   public FHashMap( final int expected, float loadfactor ) {
      super(expected, loadfactor);
   }
}
