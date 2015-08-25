package io.github.htools.graph;

import io.github.htools.lib.MathTools;

/**
 *
 * @author jeroen
 */
public class Edge<NODE, ATTR> {
    NODE source;
    NODE dest;
    ATTR attr;
    ATTR attr_to_source;
    
    public Edge(NODE source, NODE dest, ATTR attr) {
        this.source = source;
        this.dest = dest;
        this.attr = attr;
    }
    
    public Edge(NODE source, NODE dest) {
        this.source = source;
        this.dest = dest;
    }
    
    public ATTR getAttrUndirected() {
        return attr;
    }
    
    public boolean hasAttributes() {
        return attr != null || attr_to_source != null;
    }
    
    public ATTR setAttrUndirected(ATTR attr) {
        ATTR old = this.attr;
        this.attr = attr;
        return old;
    }
    
    public ATTR getAttrTo(NODE node) {
        if (source.equals(node))
            return attr_to_source;
        if (dest.equals(node))
            return attr;
        return null;
    }
    
    public ATTR setAttrTo(NODE node, ATTR attr) {
        ATTR old = null;
        if (source.equals(node)) {
            old = attr_to_source;
            attr_to_source = attr;
        }
        if (dest.equals(node)) {
            old = this.attr;
            this.attr = attr;
        }
        return old;
    }
    
    @Override
    public int hashCode() {
        int h1 = source.hashCode();
        int h2 = dest.hashCode();
        return (h1 < h2)?MathTools.combineHash(h1, h2):MathTools.combineHash(h2, h1);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Edge) {
            Edge<NODE, ATTR> oo = (Edge)o;
            if (source.equals(oo.source)) {
                return dest.equals(oo.dest);
            } else if (source.equals(oo.dest)) {
                return dest.equals(oo.source);
            }
        }
        return false;
    }
    
}
