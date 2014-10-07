package io.github.repir.tools.Collection;

import java.util.Comparator;
import java.util.Iterator;

public class PeekIterator<T> implements Iterator<T> {

   public Iterator<T> data;
   public T next, current;

   public PeekIterator(Iterator<T> data) {
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
}
