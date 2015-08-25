package io.github.htools.collection;

import java.util.Iterator;
import java.util.Queue;

public class QueueIterator<T> implements Iterator<T> {

   public Queue<T> data;

   public QueueIterator(Queue<T> data) {
      this.data = data;
   }

   @Override
   public boolean hasNext() {
      return !data.isEmpty();
   }

   @Override
   public T next() {
      return data.poll();
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
