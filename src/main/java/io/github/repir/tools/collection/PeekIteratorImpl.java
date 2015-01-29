package io.github.repir.tools.collection;

import java.util.Iterator;

public class PeekIteratorImpl<T> implements PeekIterator<T> {

   public Iterator<T> data;
   public T next, current;

   public PeekIteratorImpl(Iterator<T> data) {
      this.data = data;
      next = null;
      next();
   }

   @Override
   public boolean hasNext() {
      return next != null;
   }

   @Override
   public T next() {
      current = next;
      next = null;
      while (data.hasNext() && next == null) {
         next = data.next();
      }
      return current;
   }

   public T peek() {
      return next;
   }

   public T current() {
      return current;
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

    @Override
    public Iterator<T> iterator() {
        return this;
    }
}
