package io.github.repir.tools.DataTypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class MultiIterator<T extends Comparable<T>> implements Iterator<PeekIteratorComparable<T>> {

   public TreeSet<PeekIteratorComparable<T>> lists = new TreeSet<PeekIteratorComparable<T>>();
   public PeekIteratorComparable<T> current;

   public MultiIterator() {
   }

   public void add(PeekIteratorComparable<T> list) {
      if (list.hasNext()) {
         lists.add(list);
      }
   }

   public void reset() {
      lists.clear();
   }

   public boolean hasNext() {
      return lists.size() > 0;
   }

   public Iterator<PeekIteratorComparable<T>> peekIterator() {
      return lists.iterator();
   }

   public PeekIteratorComparable<T> next() {
      current = lists.pollFirst();
      current.next();
      add(current);
      return current;
   }

   public PeekIteratorComparable<T> current() {
      return current;
   }

   public int countIterators() {
      return lists.size();
   }

   public T peekFirst() {
      return lists.first().peek();
   }

   public T peekLast() {
      return lists.last().peek();
   }

   public void remove() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
