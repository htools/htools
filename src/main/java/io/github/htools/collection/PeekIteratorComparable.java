package io.github.htools.collection;

import java.util.Iterator;

public class PeekIteratorComparable<T extends Comparable<T>> extends PeekIteratorImpl<T> implements Comparable<PeekIteratorComparable<T>> {


   public PeekIteratorComparable(Iterator<T> data) {
       super(data);
   }

   @Override
   public int compareTo(PeekIteratorComparable<T> o) {
      return this.peek().compareTo(o.peek());
   }
}
