package io.github.htools.collection;

import io.github.htools.lib.IteratorIterable;
import java.util.Iterator;

public class ListIterator<T> implements IteratorIterable<T> {

   private Iterator<T> iter;

   public ListIterator(Iterator<T> iter) {
       this.iter = iter;
   }
   
   public ListIterator(Iterable<T> iterable) {
       this.iter = iterable.iterator();
   }

   @Override
   public boolean hasNext() {
      return iter.hasNext();
   }
   
   @Override
   public T next() {
      return iter.next();
   }

   @Override
   public void remove() {
      iter.remove();
   }

    @Override
    public Iterator<T> iterator() {
        return this;
    }
}
