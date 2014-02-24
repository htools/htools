package io.github.repir.tools.DataTypes;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;
import io.github.repir.tools.Lib.Log; 
import io.github.repir.tools.Lib.RandomTools;

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
  
  public static class StringComparator implements Comparator<String> {
      @Override
      public int compare(String o1, String o2) {
         return o1.compareTo(o2);
      }
   
}
  
   public static void main(String[] args) {
      String l[] = new String[1000000];
      for (int i = 0; i < l.length; i++) 
         l[i] = RandomTools.uuid();
      TopK2<String> topk = new TopK2<String>(10000, new StringComparator());
      log.startTime();
      for (String s : l) {
         topk.add(s);
      }
      log.reportTime("1");
      
      
   }
}
