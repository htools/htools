package io.github.repir.tools.Collection;
import io.github.repir.tools.Collection.PersistentStream.PSElement;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * To use this Deque, elements must implement the PSElement interface, which 
 * specifies a method to determine if an element from the front should be removed
 * based on the element inserted. This is useful to maintain a timeseries.
 * @author Jeroen Vuurens
 */
public class PersistentStream<T extends PSElement> extends ArrayDeque<T> {
  public static Log log = new Log( PersistentStream.class );

  public PersistentStream() {
     super();
  }

  @Override
  public boolean add(T t) {
     while (this.getFirst().remove(t)) {
         this.pollFirst();
     }
     return super.add(t);
  }
   
  @Override
  public void addLast(T t) {
     while (this.getFirst().remove(t)) {
         this.pollFirst();
     }
     super.addLast(t);
  }
   
   public PeekIterator<T> peekIterator() {
       return new PeekIterator(iterator());
   }
   
   public static interface PSElement<S> {
       boolean remove(S last);
   }
   
}
