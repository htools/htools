package io.github.htools.collection;

import java.util.Iterator;
import java.util.Map;

public class MapValueIterator<K, V> implements MixedIterator<V> {

   public Iterator<? extends Map.Entry<K, V>> data;

   public MapValueIterator(Iterator<? extends Map.Entry<K, V>> data) {
      this.data = data;
   }

   @Override
   public boolean hasNext() {
      return data.hasNext();
   }

   @Override
   public V next() {
      if (hasNext())
         return data.next().getValue();
      return null;
   }

   @Override
   public void remove() {
      data.remove();
   }

    @Override
    public Iterator<V> iterator() {
        return this;
    }
}
