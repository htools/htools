package io.github.repir.tools.DataTypes;

import io.github.repir.tools.Lib.Log;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.tools.DataTypes.TreeMapComparable.TYPE;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class TreeMapComparator<K,V> extends TreeMap<K,V> {

   public static Log log = new Log(TreeMapComparator.class);
   
   public TreeMapComparator( Comparator comparator, TYPE t) {
      super(new defaultComparator( comparator, t));
   }
   
   public TreeMapComparator( Comparator comparator, TYPE t, Map<K, V> collection ) {
      super(new defaultComparator( comparator, t));
      this.putAll(collection);
   }
   
   static class defaultComparator implements Comparator<Object> {
      private boolean desc;
      private boolean unique;
      private Comparator comparator;
      
      public defaultComparator( Comparator comparator, TYPE t ) {
         switch (t) {
            case UNIQUEASCENDING :
               desc = false;
               unique = true;
               break;
            case UNIQUEDESCENDING:
               desc = true;
               unique = true;
               break;
            case DUPLICATESASCENDING:
               desc = false;
               unique = false;
               break;
            case DUPLICATESDESCENDING:
               desc = true;
               unique = false;
         }
         this.comparator = comparator;
      }
      
      @Override
      public int compare(Object a, Object b) {
         int c = comparator.compare(a, b);
         if (desc)
            c = -c;
         if (c == 0 && !unique)
            c = 1;
         return c;
      }
   }
   
}
