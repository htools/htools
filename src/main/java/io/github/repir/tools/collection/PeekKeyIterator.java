package io.github.repir.tools.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

public class PeekKeyIterator<K, V> implements PeekIterator<K> {

   public Iterator<Map.Entry<K,V>> data;
   private K current, next;

   public PeekKeyIterator(Iterator<Map.Entry<K, V>> data) {
      this.data = data;
      next = null;
      next();
   }

   @Override
   public boolean hasNext() {
      return next != null;
   }

   @Override
   public K next() {
      current = next;
      next = null;
      while (data.hasNext() && next == null) {
         next = (K)data.next().getKey();
      }
      return current;
   }

   public K peek() {
      return next;
   }

   public K current() {
      return current;
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("Not possible on peekiterator.");
   }

    @Override
    public Iterator<K> iterator() {
        return this;
    }
}
