package io.github.repir.tools.graph;

/**
 *
 * @author jeroen
 */
public class GraphDouble<N> extends Graph<N, Double> {

    public void add(N source, N dest, double d) {
        Edge<N, Double> edge = getEdge(source, dest);
        if (edge != null) {
           edge.setAttrUndirected(edge.getAttrUndirected() + d);
        } else {
           setUndirected(source, dest, d);
        }
    }
}
