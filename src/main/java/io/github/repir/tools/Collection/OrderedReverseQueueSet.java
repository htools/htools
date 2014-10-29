package io.github.repir.tools.Collection;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.RandomTools;
import java.util.Arrays; 
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * This collection uses a PriorityQueue to retrieve the first ordered item. Different
 * from other Collections, on iteration the items are removed from the set,
 * if iteration is terminated the remainder of items remain unsorted, but iteration
 * can be continued on the remainder of the set.
 * @author Jeroen Vuurens
 */
public class OrderedReverseQueueSet<T> extends OrderedQueueSet<T> {
  public static Log log = new Log( OrderedReverseQueueSet.class );

  public OrderedReverseQueueSet(Collection<T> collection) {
     super(collection.size(), collection, new StdComparator());
  }

  public OrderedReverseQueueSet(int k) {
     super(k, new StdComparator());
  }

  public OrderedReverseQueueSet() {
     this(11);
  }
  
  public static class StdComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            return ((Comparable)o2).compareTo(o1);
        }
  }
  
   public static void main(String[] args) {
      String l[] = new String[1000000];
      for (int i = 0; i < l.length; i++) 
         l[i] = RandomTools.uuid();
      OrderedReverseQueueSet<String> topk = new OrderedReverseQueueSet<String>(10000);
      for (String s : l) {
         topk.add(s);
      }

      String last = "z";
      for (String s : topk) {
          if (s.compareTo(last) > 0)
              log.info("FAULT %s", s);
          last = s;
      }
      
   }
}
