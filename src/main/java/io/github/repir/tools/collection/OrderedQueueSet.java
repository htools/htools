package io.github.repir.tools.collection;
import io.github.repir.tools.collection.QueueIterator;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.RandomTools;
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
public class OrderedQueueSet<T> extends PriorityQueue<T> {
  public static Log log = new Log( OrderedQueueSet.class );
  final Comparator<? super T> comparator;

  public OrderedQueueSet(int k, Comparator<? super T> comparator) {
     super(k, comparator);
     this.comparator = comparator;
  }

  public OrderedQueueSet(Comparator<? super T> comparator) {
     this(11, comparator);
  }

  public OrderedQueueSet(int k, Collection<T> collection, Comparator<? super T> comparator) {
     this(k, comparator);
     this.addAll(collection);
  }

  public OrderedQueueSet(Collection<T> collection, Comparator<? super T> comparator) {
     this(collection.size(), collection, comparator);
  }

  public OrderedQueueSet(Collection<T> collection) {
     this(collection.size(), collection, new StdComparator());
  }

  public OrderedQueueSet(int k) {
     this(k, new StdComparator());
  }

  public OrderedQueueSet() {
     this(11);
  }

  @Override
  public boolean add(T t) {
     boolean r = false;
     r = super.add(t);
     return r;
  }
  
  public T[] sorted() {
      T[] toArray;
      toArray = (T[])this.toArray();
      Arrays.sort(toArray, comparator);
      return toArray;
  }
   
  @Override
  public Iterator<T> iterator() {
     return new QueueIterator(this); 
  }
  
  public Iterator<T> nonRemoveIterator() {
     return super.iterator();
  }
  
  public static class StdComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            return ((Comparable)o1).compareTo(o2);
        }
  }
  
   public static void main(String[] args) {
      String l[] = new String[1000000];
      for (int i = 0; i < l.length; i++) 
         l[i] = RandomTools.uuid();
      OrderedQueueSet<String> topk = new OrderedQueueSet<String>(10000);
      for (String s : l) {
         topk.add(s);
      }

      String last = "\0";
      for (String s : topk) {
          if (s.compareTo(last) < 0)
              log.info("FAULT %s", s);
          last = s;
      }
      
   }
}
