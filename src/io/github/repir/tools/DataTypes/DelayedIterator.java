package io.github.repir.tools.DataTypes;
import io.github.repir.tools.Lib.Log;
import java.util.PriorityQueue;

/**
 * Iterates through a set, providing simultaneous inspection f the current and next item.
 * @author Jeroen Vuurens
 */
public class DelayedIterator<T extends Comparable<T>> extends PriorityQueue<T> {
  public static Log log = new Log( DelayedIterator.class );
  private T current;

  public DelayedIterator() {
     super();
  }
  
  public T next() {
     return current = poll();
  }
  
  public T current() {
     return current;
  }
  
  public boolean hasNext() {
     return size() > 0;
  }
  
  public boolean proceedTo(T t) {
     while (hasNext() && peek().compareTo(t) <= 0) {
        next();
     }
     return current.compareTo(t) == 0;
  }
}
