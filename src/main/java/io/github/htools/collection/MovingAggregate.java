package io.github.htools.collection;

import io.github.htools.collection.MovingAggregate.Element;
import io.github.htools.lib.Log;

import java.util.ArrayDeque;

/**
 *
 * @author jeroen
 */
public class MovingAggregate<T extends Element> extends ArrayDeque<T> {
    public static Log log = new Log(MovingAggregate.class);
    
    public MovingAggregate() { }

    @Override
    public boolean add(T t) {
        if (size() == 0) {
            super.add(t);
        } else {
            T last = peekLast();
            if (last.equals(t))
                last.aggregate(t);
            else
                super.add(t);
        }
       T first = peekFirst();
       while (first.remove(this)) {
           this.pollFirst();
           first = this.peekFirst();
           if (first == null) {
               return false;
           }
       }
       return true;
    }
    
    public T aggregate() {
        if (size() == 0)
            return null;
        T aggregate = (T)peekFirst().aggregateInstance(this);
        for (T t : this)
            aggregate.aggregate(t);
        return aggregate;
    }
    
    public static interface Element<S extends Element> {
        public boolean isEqual(S o);
        public void aggregate(S s);
        public boolean remove(MovingAggregate<S> ma);
        public S aggregateInstance(MovingAggregate<S> ma);
    }
}
