package io.github.htools.collection;

import io.github.htools.collection.PersistentStream.PSElement;
import io.github.htools.lib.Log;

import java.util.ArrayDeque;

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
   
   public PeekIteratorImpl<T> peekIterator() {
       return new PeekIteratorImpl(iterator());
   }
   
   public static interface PSElement<S> {
       boolean remove(S last);
   }
   
}
