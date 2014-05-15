package io.github.repir.tools.DataTypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class MultiIterator<T extends Comparable<T>> implements Iterator<PeekIterator<T>> {

   public TreeSet<PeekIterator<T>> lists = new TreeSet<PeekIterator<T>>();
   public PeekIterator<T> current;

   public MultiIterator() {
   }

   public void add(PeekIterator<T> list) {
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

   public Iterator<PeekIterator<T>> peekIterator() {
      return lists.iterator();
   }

   public PeekIterator<T> next() {
      current = lists.pollFirst();
      current.next();
      add(current);
      return current;
   }

   public PeekIterator<T> current() {
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
