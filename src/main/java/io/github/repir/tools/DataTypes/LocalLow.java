package io.github.repir.tools.DataTypes;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.RandomTools;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;

/**
 * This collection retains the last K objects that are added, and efficiently
 * returns the highest of the last K objects. Instantiate with any
 * k > 0, where k is the maximum number of objects to keep, and a Comparator, 
 * i.e. the smallest element should return -1. Alternatively, this can be used
 * to obtain the lowest value by inverting the comparator.
 * @author Jeroen Vuurens
 */
public class LocalLow<T> extends LocalHigh<T> {

  public LocalLow(int k, Comparator<? super T> comparator) {
     super(k, new StdComparator2(comparator));
  }

  public LocalLow(int k) {
     super(k, new StdComparator());
  }
  
  public static class StdComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            return -((Comparable)o1).compareTo(o2);
        }   
  }
  
  public static class StdComparator2 implements Comparator {
      Comparator comparator;
      
      public StdComparator2(Comparator a) {
          comparator = a;
      }
      
        @Override
        public int compare(Object o1, Object o2) {
            return -comparator.compare(o1, o2);
        }   
  }
  
   public static void main(String[] args) {
      int k = 10;
      int l[] = new int[1000000];
      for (int i = 0; i < l.length; i++) 
         l[i] = RandomTools.getInt();
      LocalLow<Integer> locallow = new LocalLow<Integer>(10);
      log.startTime();
      for (int i = 0; i < l.length; i++) {
         locallow.add(l[i]);
         int low = Integer.MAX_VALUE;
         for (int j = Math.max(0, i - k + 1); j <= i; j++)
             low = Math.min(low, l[j]);
         if (locallow.getMax() != low)
             log.info("error %d %d", low, locallow.getMax());
      }
   }
}
