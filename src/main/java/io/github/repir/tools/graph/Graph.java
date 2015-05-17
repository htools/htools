package io.github.repir.tools.graph;

import io.github.repir.tools.collection.HashMapSet;
import java.util.HashMap;

/**
 *
 * @author jeroen
 */
public class Graph<N, A> {

    HashMapSet<N, Edge<N, A>> nodes = new HashMapSet();
    HashMap<Edge<N, A>, Edge<N, A>> edges = new HashMap();

    public Graph() {
    }

    public void addNode(N node) {
        nodes.getSet(node);
    }

    public boolean contains(N node) {
        return nodes.containsKey(node);
    }
    
    public NodeIterator<N, A> getIteratorUndirected(N source) {
        return new NodeIterator(source, nodes.get(source));
    }
    
    public NodeIterator<N, A> getIteratorDirected(N source) {
        return new NodeIteratorDirected(source, nodes.get(source));
    }
    
    protected Edge<N, A> getEdge(N source, N dest) {
        return edges.get(new Edge(source, dest));
    }
    
    protected Edge<N, A> createEdge(N source, N dest) {
        Edge<N, A> edge = new Edge(source, dest);
        Edge<N, A> existing = edges.get(edge);
        if (existing == null) {
            nodes.add(source, edge);
            nodes.add(dest, edge);
            edges.put(edge, edge);
            return edge;
        }
        return existing;
    }
    
    public A getUndirected(N source, N dest) {
        Edge<N, A> edge = getEdge(source, dest);
        if (edge != null) {
            return edge.getAttrUndirected();
        }
        return null;
    }

    public A getDirected(N source, N dest) {
        Edge<N, A> edge = getEdge(source, dest);
        if (edge != null) {
            return edge.getAttrTo(dest);
        }
        return null;
    }

    public void setUndirected(N source, N dest, A attr) {
        Edge<N, A> edge = createEdge(source, dest);
        edge.setAttrUndirected(attr);
    }

    public void SetDirected(N source, N dest, A attr) {
        Edge<N, A> edge = createEdge(source, dest);
        edge.setAttrTo(dest, attr);
    }

    public A removeUndirected(N source, N dest) {
        Edge<N, A> edge = edges.remove(new Edge(source, dest));
        if (edge != null) {
            nodes.get(source).remove(edge);
            nodes.get(dest).remove(edge);
            return edge.getAttrUndirected();
        }
        return null;
    }
    
    public A removeDirected(N source, N dest) {
        Edge<N, A> edge = getEdge(source, dest);
        if (edge != null) {
            nodes.get(source).remove(edge);
            A attr = edge.getAttrTo(dest);
            if (attr != null) {
                edge.setAttrTo(dest, null);
                if (!edge.hasAttributes())
                   edges.remove(edge);
            }
            return attr;
        }
        return null;
    }
}
