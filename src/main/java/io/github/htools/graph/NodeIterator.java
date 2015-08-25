package io.github.htools.graph;

import io.github.htools.lib.Log;
import io.github.htools.type.Tuple2;
import java.util.HashSet;
import java.util.Iterator;
/**
 *
 * @author jeroen
 */
public class NodeIterator<N, A> implements Iterable<Tuple2<N, A>>, Iterator<Tuple2<N, A>> {
   public static final Log log = new Log( NodeIterator.class );
   protected final N source;
   protected final Iterator<Edge<N, A>> iter;
   
   public NodeIterator(N source, Iterable<Edge<N, A>> iterable) {
       this.source = source;
       this.iter = iterable.iterator();
   }
   
    @Override
    public Iterator<Tuple2<N, A>> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public Tuple2<N, A> next() {
        Edge<N, A> next = iter.next();
        if (next != null) {
            if (next.source.equals(source))
               return new Tuple2<N, A>(next.dest, next.attr);
            if (next.dest.equals(source))
               return new Tuple2<N, A>(next.source, next.attr);
        }
        return null;
    }
    
   @Override
    public void remove() {
        iter.remove();
    }
}
