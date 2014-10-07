package io.github.repir.tools.Collection;
import io.github.repir.tools.Collection.TopKV.Entry;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.RandomTools; 
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * This collection retains the TopK key value pairs that are added. Instantiate with any
 * int k > 0, where k is the maximum number of objects to keep, and a inverse 
 * Comparator, i.e. the smallest element should return -1. TopK does not sort
 * the objects, however, sorted will return a sorted array.
 * @author Jeroen Vuurens
 */
public class TopKV<K, V> extends PriorityQueue<Entry> {
  public static Log log = new Log( TopKV.class );
  final int k;
  Entry lowest;
  Entry highest;

  public TopKV(int k, Comparator<? super K> comparator) {
     super(k, new KeyComparator(comparator));
     this.k = k;
  }

  public TopKV(int k) {
     super(k);
     this.k = k;
  }

  public boolean add(K key, V value) {
     boolean r = false;
     Entry e = new Entry(key, value);
     if (size() < k) {
        r = super.add(e);
        if (lowest == null || compare(e, lowest) < 0)
           lowest = e;
        if (highest == null || compare(e, highest) > 0)
           highest = e;
     } else if (compare(e, lowest) > 0) {
        this.poll();
        r = super.add(e);
        lowest = this.peek();
        if (compare(e, highest) > 0)
           highest = e;
     }
     return r;
  }
  
  public boolean containsKey(K key) {
      for (Entry entry : this) 
          if (entry.key.equals(key))
              return true;
      return false;
  }
  
  public int compare(Entry a, Entry b) {
      if (comparator() == null)
          return a.compareTo(b);
      else
          return comparator().compare(a, b);
  }
  
  public Entry[] sorted() {
      Entry[] toArray = this.toArray(new TopKV.Entry[size()]);
      if (comparator() == null)
         Arrays.sort(toArray);
      else
          Arrays.sort(toArray, comparator());
      return toArray;
  }
   
  protected static class KeyComparator implements Comparator<TopKV.Entry> {
        private Comparator c;
        
        public KeyComparator(Comparator c) {
            this.c = c;
        }

        @Override
        public int compare(TopKV.Entry o1, TopKV.Entry o2) {
            return c.compare(o1.key, o2.key);
        }
  }
  
  public Entry first() {
      return highest;
  }
  
  public Entry last() {
      return lowest;
  }
  
   public static void main(String[] args) {
      Integer l[] = new Integer[10000];
      for (int i = 0; i < l.length; i++) 
         l[i] = RandomTools.getInt(10000);
      TopKV<Integer, Integer> topk = new TopKV<Integer, Integer>(1000);
      for (Integer s : l) {
         topk.add(s, s);
      }
      //for (String s : topk)
      //   log.reportTime("%s", s);
      log.info("topk lowst %s highest %s", topk.lowest.key, topk.highest.key);
      for (Integer s : l) {
          if (s.compareTo((Integer)topk.lowest.key) > 0 && !topk.containsKey(s))
              log.info("FAULT %s", s);
      }
   }
   
   public class Entry implements Comparable<Entry>{
       K key;
       V value;
       
       Entry(K key, V value) {
           this.key = key;
           this.value = value;
       }

       @Override
       public String toString() {
           return new StringBuilder().append(key.toString()).append("=").append(value.toString()).toString();
       }
       
       @Override
       public boolean equals(Object o) {
           if (o instanceof TopKV.Entry) {
               Entry e = (Entry)o;
               return e.key.equals(key) && e.value.equals(value);
           }
           return false;
       }
       
       @Override
       public int hashCode() {
           return key.hashCode();
       }
       
        @Override
        public int compareTo(Entry o) {
            Comparable a = (Comparable)key;
            return a.compareTo(o.key);
        }     
   }
}
