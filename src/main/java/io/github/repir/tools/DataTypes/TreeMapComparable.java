package io.github.repir.tools.DataTypes;

import io.github.repir.tools.Lib.Log;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class TreeMapComparable<K extends Comparable,V> extends TreeMap<K,V> {

   public static Log log = new Log(TreeMapComparable.class);
   public static enum TYPE {
      UNIQUEASCENDING,
      UNIQUEDESCENDING,
      DUPLICATESASCENDING,
      DUPLICATESDESCENDING
   }
   
   public TreeMapComparable( TYPE t ) {
      super(new defaultComparable( t ));
   }
   
   public TreeMapComparable( TYPE t, Comparator c ) {
      super(new defaultComparator( c, t ));
   }
   
   public TreeMapComparable( TYPE t, Map<K, V> collection ) {
      super(new defaultComparable( t ));
      this.putAll( collection );
   }
   
   public TreeMapComparable( TYPE t, Comparator c, Map<K, V> collection ) {
      super(new defaultComparator( c, t ));
      this.putAll( collection );
   }
   
   static class defaultComparable implements Comparator<Comparable> {
      private boolean desc;
      private boolean unique;
      
      public defaultComparable( TYPE t ) {
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
      }
      
      @Override
      public int compare(Comparable a, Comparable b) {
         int c = a.compareTo(b);
         if (desc)
            c = -c;
         if (c == 0 && !unique)
            c = 1;
         return c;
      }
   }
   
   static class defaultComparator implements Comparator {
      private boolean desc;
      private boolean unique;
      private Comparator comparator;
      
      public defaultComparator( Comparator c, TYPE t ) {
         this.comparator = c;
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
