package io.github.repir.tools.collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;
import io.github.repir.tools.lib.Log; 
import io.github.repir.tools.lib.RandomTools;

/**
 *
 * @author Jeroen Vuurens
 */
public class TopK2<T> extends TreeSet<T> {
  public static Log log = new Log( TopK2.class );
  final Comparator<? super T> comparator;
  final int k;
  T lowest;

  public TopK2(int k, Comparator<? super T> comparator) {
     super(comparator);
     this.comparator = comparator;
     this.k = k;
  }

  public TopK2(int k) {
     this(k, new TopK.StdComparator());
  }

  @Override
  public boolean add(T t) {
     boolean r = false;
     if (size() < k) {
        r = super.add(t);
        if (lowest == null || comparator.compare(t, lowest) < 0)
           lowest = t;
     } else if (comparator.compare(t, lowest) > 0) {
        this.pollLast();
        r = super.add(t);
        lowest = this.last();
     }
     return r;
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
      TopK2<String> topk = new TopK2<String>(10000);
      for (String s : l) {
         topk.add(s);
      }    
   }
}
