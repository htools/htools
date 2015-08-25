package io.github.htools.collection;

import java.util.Iterator;
import java.util.Map;

public class PeekValueIterator<K, V> implements PeekIterator<V> {

   public Iterator<? extends Map.Entry<K, V>> data;
   private V current, next;

   public PeekValueIterator(Iterator<? extends Map.Entry<K, V>> data) {
      this.data = data;
      next = null;
      next();
   }

   @Override
   public boolean hasNext() {
      return next != null;
   }

   @Override
   public V next() {
      current = next;
      next = null;
      while (data.hasNext() && next == null) {
         next = data.next().getValue();
      }
      return current;
   }

   public V peek() {
      return next;
   }

   public V current() {
      return current;
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("Not possible on peekiterator.");
   }

    @Override
    public Iterator<V> iterator() {
        return this;
    }
}
