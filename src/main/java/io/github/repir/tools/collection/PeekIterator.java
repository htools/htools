package io.github.repir.tools.collection;

import java.util.Iterator;
/**
 * An Iterator that allows to look ahead at the next value. This type of Iterator
 * does not support remove. 
 * @author jeroen
 */
public interface PeekIterator<T> extends Iterator<T>, Iterable<T> {
   public T peek();

   public T current();
}
