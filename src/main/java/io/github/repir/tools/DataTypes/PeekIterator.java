package io.github.repir.tools.DataTypes;

import java.util.Iterator;

public class PeekIterator<T extends Comparable<T>> implements Iterator<PeekIterator<T>>, Comparable<PeekIterator<T>> {

   public Iterator<T> data;
   public int id;
   public T next, current;

   public PeekIterator(int id, Iterator<T> data) {
      this.data = data;
      this.id = id;
      next = null;
      next();
   }

   public boolean hasNext() {
      return next != null;
   }

   public PeekIterator<T> next() {
      current = next;
      next = null;
      while (data.hasNext() && next == null) {
         next = data.next();
      }
      return this;
   }

   public T peek() {
      return next;
   }

   public void remove() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int compareTo(PeekIterator<T> o) {
      return this.peek().compareTo(o.peek());
   }
}
