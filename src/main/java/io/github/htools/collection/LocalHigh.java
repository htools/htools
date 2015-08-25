package io.github.htools.collection;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.RandomTools;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;

/**
 * This collection retains the last K objects that are added, and efficiently
 * returns the highest of the last K objects. Instantiate with any
 * k &gt; 0, where k is the maximum number of objects to keep, and a Comparator, 
 * i.e. the smallest element should return -1. Alternatively, this can be used
 * to obtain the lowest value by inverting the comparator.
 * @author Jeroen Vuurens
 */
public class LocalHigh<T> {
  public static Log log = new Log( LocalHigh.class );
  final Comparator<? super T> comparator;
  final int k;
  T array[];
  private int pointer = 0;
  private int size = 0;
  T highest;

  public LocalHigh(int k, Comparator<? super T> comparator) {
     this.comparator = comparator;
     this.k = k;
  }

  public LocalHigh(int k) {
     this.comparator = new StdComparator();
     this.k = k;
  }

  public void add(T t) {
     if (array == null) {
        array = (T[])ArrayTools.createArray(t.getClass(), k);
     }
     if (size < k) {
        array[pointer] = t;
        pointer = (pointer + 1) % k;
        size++;
        if (highest == null || comparator.compare(t, highest) > 0)
           highest = t;
     } else {
        T remove = array[pointer];
        array[pointer] = t;
        pointer = (pointer + 1) % k;
        if (comparator.compare(t, highest) > 0) {
            highest = t;
        } else if (comparator.compare(remove, highest) == 0) {
            highest = array[0];
            for (int i = 1; i < k; i++) {
                if (comparator.compare(array[i], highest) > 0) {
                    highest = array[i];
                }
            }
        }
     }
  }
  
  public T pollLast() {
     size--;
     pointer = (pointer - 1 + k)%k;
     return array[pointer]; 
  }
  
  public T pollFirst() {
     int p = (pointer - size + k) % k;
     size--;
     return array[p]; 
  }
  
  public T getMax() {
      return highest;
  }
  
  public class StdComparator implements Comparator<T> {
        @Override
        public int compare(T o1, T o2) {
            return ((Comparable)o1).compareTo(o2);
        }
      
  }
  
  public static class IntComparator implements Comparator<Integer> {
      @Override
      public int compare(Integer o1, Integer o2) {
         return o1.compareTo(o2);
      }
   
}
  
   public static void main(String[] args) {
      int k = 10;
      int l[] = new int[1000000];
      for (int i = 0; i < l.length; i++) 
         l[i] = RandomTools.getInt();
      LocalHigh<Integer> topk = new LocalHigh<Integer>(10);
      for (int i = 0; i < l.length; i++) {
         topk.add(l[i]);
         int high = Integer.MIN_VALUE;
         for (int j = Math.max(0, i - k + 1); j <= i; j++)
             high = Math.max(high, l[j]);
         if (topk.getMax() != high)
             log.info("error %d %d", high, topk.getMax());
      }
   }
}
