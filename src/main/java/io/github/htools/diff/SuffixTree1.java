package io.github.htools.diff;

import io.github.htools.lib.Log;
import java.io.*;
import java.util.*;

public class SuffixTree1 {

    static Log log = new Log(SuffixTree1.class);
    final int oo = Integer.MAX_VALUE;
    Node[] nodes;
    char[] text;
    int position = -1,
            needSuffixLink,
            remainder;

    int active_length, active_edge;
    Node active_node, root, currentNode;

    class RootNode extends Node {

        public RootNode() {
            super(null, -1, -1);
        }

        public String toString() {
            return "";
        }

        public String fullString() {
            return "";
        }
    }

    class Node {

        /*
         There is no need to create an "Edge" class.
         Information about the edge is stored right in the node.
         [start; end) interval specifies the edge,
         by which the node is connected to its parent node.
         */
        int start;
        int end;
        Node link;
        Node parent;
        public HashMap<Character, Node> next = new HashMap<Character, Node>();

        public Node(Node parent, int start, int end) {
            this.start = start;
            this.end = end;
            this.parent = parent;
        }

        public char charAt(int posOnEdge) {
            return text[start + posOnEdge];
        }

        public int edgeLength() {
            return Math.min(end, position + 1) - start;
        }

        @Override
        public String toString() {
            return new String(Arrays.copyOfRange(text, start, Math.min(position + 1, end)));
        }

        public String fullString() {
            return (parent == root) ? toString() : parent.fullString() + "-" + toString();
        }
    }

    public SuffixTree1(int length) {
        nodes = new Node[2 * length + 2];
        text = new char[length];
        root = current = new RootNode();
    }

    public SuffixTree1(String content) throws Exception {
        this(content.length());
        for (char c : content.toCharArray()) {
            this.addChar1(c);
        }
    }

    Node newNode(Node parent, int start, int end) {
        return new Node(parent, start, end);
    }

    Node current;
    int taken = 0;

    public void addChar1(char c) {
        text[++position] = c;
        if (current.edgeLength() - taken > 0) {
            if (c == current.charAt(taken)) {
                // already have
                log.info("already have in node '%s' '%s' %d", c, current.toString(), taken);
                taken++;
                return;
            }
        }
        if (current.edgeLength() >= taken) {
            Node child = current.next.get(c);
            if (child != null) {
                // already have in child node
                log.info("already have child '%s' '%s' %d", c, child.toString(), 0);
                taken = 1;
                current = child;
                return;
            }
        }
        // doesn't exist, must add
        if (current.edgeLength() > taken) {
            // split somewhere in the middle
            log.info("split inside '%s' '%s' '%s' %d", c, current.fullString(), current.toString(), taken);
            Node split = newNode(current.parent, current.start, current.start + taken);
            Node newleaf = newNode(split, position, Integer.MAX_VALUE);
            split.next.put(newleaf.charAt(0), newleaf);
            Node replaced = current.parent.next.put(split.charAt(0), split);
            if (replaced.link != null) {
                split.link = split(replaced.link, taken);
            }
            Node leaf = newNode(split, current.start + taken, current.end);
            split.next.put(leaf.charAt(0), leaf);
            for (taken--; taken >= 0; taken--)
                 addRoot(taken);
        } else {
            log.info("add child '%s' '%s' '%s'", c, current.fullString(), current.toString());
            Node leaf = newNode(current, position, Integer.MAX_VALUE);
            current.next.put(c, leaf);
            while (current.link != null) {
                log.info("follow link '%s' '%s'", current.fullString(), current.link.fullString());
                current = current.link;
                leaf = current.next.get(c);
                if (leaf == null) {
                    leaf = newNode(current, position, Integer.MAX_VALUE);
                    current.next.put(c, leaf);
                } else {
                    break;
                }
            }
            if (current.link == null) {
                for (taken--; taken >= 0; taken--)
                    addRoot(taken);
            }
        }
        current = root;
        taken = 0;
    }

    public void addNode(Node current, int taken) {
        if (current.edgeLength() > taken) {
            // split somewhere in the middle
            log.info("split inside '%s' '%s' '%s' %d", text[position], current.fullString(), current.toString(), taken);
            Node split = newNode(current.parent, current.start, current.start + taken);
            Node newleaf = newNode(split, position, Integer.MAX_VALUE);
            split.next.put(newleaf.charAt(0), newleaf);
            Node replaced = current.parent.next.put(split.charAt(0), split);
            if (replaced.link != null) {
                split.link = split(replaced.link, taken);
            }
            Node leaf = newNode(split, current.start + taken, current.end);
            split.next.put(leaf.charAt(0), leaf);
        } else {
            log.info("add child '%s' '%s' '%s'", text[position], current.fullString(), current.toString());
            Node leaf = newNode(current, position, Integer.MAX_VALUE);
            current.next.put(text[position], leaf);
            while (current.link != null) {
                log.info("follow link '%s' '%s'", current.fullString(), current.link.fullString());
                current = current.link;
                leaf = current.next.get(text[position]);
                if (leaf == null) {
                    leaf = newNode(current, position, Integer.MAX_VALUE);
                    current.next.put(text[position], leaf);
                } else {
                    break;
                }
            }
        }

    }

    void addRoot(int length) {
        log.info("addRoot %d %d", length, root.next.size());
        int pos = position - length;
        Node node = root;
        int edge = 0;
        for (; pos < position; pos++) {
            log.info("pos %d position %d node %s edge %d edgelength %d", pos, position, node.fullString(), edge, node.edgeLength());
            if (node.edgeLength() > edge) {
                edge++;
            } else {
                length -= node.edgeLength();
                node = node.next.get(text[pos]);
                edge = 1;
            }
        }

        if (node.edgeLength() > edge || !node.next.containsKey(text[position])) {
            addNode(node, length);
        }
        log.info("exit addRoot %d", root.next.size());
    }
    
    Node split(Node splitthis, int length) {
        log.info("split '%s' '%s', %d", splitthis.fullString(), splitthis.toString(), length);
        Node split = newNode(splitthis.parent, splitthis.start, splitthis.start + length);
        Node newleaf = newNode(split, position, Integer.MAX_VALUE);
        split.next.put(newleaf.charAt(0), newleaf);
        Node replaced = splitthis.parent.next.put(split.charAt(0), split);
        if (replaced.link != null) {
            split.link = split(replaced.link, length);
        }
        Node leaf = newNode(split, splitthis.start + length, splitthis.end);
        split.next.put(leaf.charAt(0), leaf);
        return split;
    }
    
    /*
     printing the Suffix Tree in a format understandable by graphviz. The output is written into
     st.dot file. In order to see the suffix tree as a PNG image, run the following command:
     dot -Tpng -O st.dot
     */
    String edgeString(int node) {
        return new String(Arrays.copyOfRange(text, nodes[node].start, Math.min(position + 1, nodes[node].end)));
    }

    HashMap<Integer, String> printTree() {
        HashMap<Integer, String> result = new HashMap();
        printTree(result, root, "");
        return result;
    }
    
    void printTree(HashMap<Integer, String>map , Node x, String current) {
        if (x.next.size() == 0) {
            log.info("%s", current);
            map.put(map.size(), current);
        } else {
            log.info("node %s childs %d", x.fullString(), x.next.size());
            for (Node child : x.next.values()) {
                printTree(map, child, current + "-" + child.toString());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String text = "to be or not to be that is the question";
        text = "abcabcabcd";
        SuffixTree1 st1 = new SuffixTree1(text);
        //SuffixTree st = new SuffixTree(text);
        //log.info("equals %b", st.printTree().equals(st1.printTree()));
        log.info("%s", st1.printTree());
    }
    
}
