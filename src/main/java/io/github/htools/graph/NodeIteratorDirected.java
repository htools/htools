package io.github.htools.graph;

import io.github.htools.lib.Log;
import io.github.htools.type.Tuple2;
/**
 *
 * @author jeroen
 */
public class NodeIteratorDirected<N, A> extends NodeIterator<N, A> {
   public static final Log log = new Log( NodeIteratorDirected.class );
   
   public NodeIteratorDirected(N source, Iterable<Edge<N, A>> iterable) {
       super(source, iterable);
   }
   
    @Override
    public Tuple2<N, A> next() {
        Edge<N, A> next = iter.next();
        if (next != null) {
            if (next.source.equals(source))
               return new Tuple2<N, A>(next.dest, next.getAttrTo(next.dest));
            if (next.dest.equals(source))
               return new Tuple2<N, A>(next.source, next.getAttrTo(next.source));
        }
        return null;
    }
}
