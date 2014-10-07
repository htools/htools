package io.github.repir.tools.Type;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import io.github.repir.tools.Lib.Log;

/**
 * Stores a pair of ints in a single HashSet of long.
 * Use iteratorPair() to iterate over the results.
 * Only works with unsigned ints atm.
 * @author jer
 */
public class IntPairSet extends HashSet<Long> {

   public static Log log = new Log(IntPairSet.class);

   public IntPairSet() {
      super();
   }

   public boolean add(int a, int b) {
      return add(getIntPair(a, b));
   }
   
   public long getIntPair(int i, int j) {
      return ((long)i << 32) | (j & 0xFFFFFFFFL);
   }

   public EntrySet entrySet() {
      return new EntrySet();
   }
   
    public final class EntrySet extends AbstractSet<Map.Entry<Integer, Integer>> {
        public Iterator<Map.Entry<Integer, Integer>> iterator() {
            return iteratorPair();
        }
        public boolean contains(Object o) {
            return IntPairSet.this.contains(o);
        }
        public boolean remove(Object o) {
            return IntPairSet.this.remove(o);
        }
        public int size() {
            return IntPairSet.this.size();
        }
        public void clear() {
            IntPairSet.this.clear();
        }
    }
    
   public Iterator<Map.Entry<Integer, Integer>> iteratorPair() {
      return new Iterator<Map.Entry<Integer, Integer>>() {
         private Iterator<Long> i = iterator();

         public boolean hasNext() {
            return i.hasNext();
         }

         public Map.Entry<Integer, Integer> next() {
            long l = i.next();
            Map.Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<Integer, Integer>((int) (l >>> 32), (int) (l & 0xFFFFFFFFL));
            return entry;
         }

         public void remove() {
            i.remove();
         }
      };
   }
}
