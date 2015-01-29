package io.github.repir.tools.collection;

import java.util.Iterator;
import java.util.Map;

public class MapKeyIterator<K, V> implements MixedIterator<K> {

   public Iterator<Map.Entry<K, V>> data;

   public MapKeyIterator(Iterator<Map.Entry<K, V>> data) {
      this.data = data;
   }

   @Override
   public boolean hasNext() {
      return data.hasNext();
   }

   @Override
   public K next() {
      if (hasNext())
         return data.next().getKey();
      return null;
   }

   @Override
   public void remove() {
      data.remove();
   }

    @Override
    public Iterator<K> iterator() {
        return this;
    }
}
