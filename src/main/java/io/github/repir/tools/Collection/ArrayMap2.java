package io.github.repir.tools.Collection;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.Log;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ArrayMap2<K, V> extends AbstractMap<K, V> implements Map<K, V> {

   public static Log log = new Log(ArrayMap2.class);
   private Set<Entry<K, V>> entries = null;
   private ArrayList<Entry<K, V>> list;

   public ArrayMap2() {
      list = new ArrayList<Entry<K, V>>();
   }

   static class Entry<K, V> implements Map.Entry<K, V> {

      protected K key;
      protected V value;

      public Entry(K key, V value) {
         this.key = key;
         this.value = value;
      }

      public K getKey() {
         return key;
      }

      public V getValue() {
         return value;
      }

      public V setValue(V newValue) {
         V oldValue = value;
         value = newValue;
         return oldValue;
      }

      public boolean equals(Object o) {
         return false;
      }

      public int hashCode() {
         int keyHash = (key == null ? 0 : key.hashCode());
         int valueHash = (value == null ? 0 : value.hashCode());
         return keyHash ^ valueHash;
      }

      public String toString() {
         return key + "=" + value;
      }
   }

   public ArrayMap2(Map map) {
      list = new ArrayList<Entry<K, V>>();
      putAll(map);
   }

   public ArrayMap2(int initialCapacity) {
      list = new ArrayList<Entry<K, V>>(initialCapacity);
   }

   public Set entrySet() {
      if (entries == null) {
         entries = new AbstractSet<Entry<K, V>>() {
            public void clear() {
               list.clear();
            }

            public Iterator iterator() {
               return list.iterator();
            }

            public int size() {
               return list.size();
            }
         };
      }
      return entries;
   }

   public V put(K key, V value) {
      int size = list.size();
      list.add(new Entry(key, value));
      return null;
   }

   public Object clone() {
      return new ArrayMap2<K, V>(this);
   }
}