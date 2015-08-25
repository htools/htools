package io.github.htools.diff;

import io.github.htools.lib.Log;
import java.io.*;
import java.util.*;

public class SuffixTree {
    static Log log = new Log(SuffixTree.class); 
    final int oo = Integer.MAX_VALUE / 2;
    Node[] nodes;
    char[] text;
    int root, position = -1,
            currentNode,
            needSuffixLink,
            remainder;

    int active_node, active_length, active_edge;

    class Node {

        /*
         There is no need to create an "Edge" class.
         Information about the edge is stored right in the node.
         [start; end) interval specifies the edge,
         by which the node is connected to its parent node.
         */
        int start, end = oo, link;
        public HashMap<Character, Integer> next = new HashMap<Character, Integer>();

        public Node(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int edgeLength() {
            return Math.min(end, position + 1) - start;
        }
        
        @Override
        public String toString() {
            return new String(Arrays.copyOfRange(text, start, Math.min(position + 1, end)));
        }
    }

    public SuffixTree(int length) {
        nodes = new Node[2 * length + 2];
        text = new char[length];
        root = active_node = newNode(-1, -1);
    }
    
    public SuffixTree(String content) throws Exception {
        this(content.length());
        for (char c : content.toCharArray()) {
            this.addChar(c);
        }
    }

    private void addSuffixLink(int node) {
        if (needSuffixLink > 0) {
            nodes[needSuffixLink].link = node;
        }
        needSuffixLink = node;
    }

    char active_edge() {
        return text[active_edge];
    }

    boolean walkDown(int next) {
        if (active_length >= nodes[next].edgeLength()) {
            active_edge += nodes[next].edgeLength();
            active_length -= nodes[next].edgeLength();
            active_node = next;
            return true;
        }
        return false;
    }

    int newNode(int start, int end) {
        nodes[++currentNode] = new Node(start, end);
        return currentNode;
    }

    public void addChar(char c) throws Exception {
        text[++position] = c;
        needSuffixLink = -1;
        remainder++;
        while (remainder > 0) {
            if (active_length == 0) {
                active_edge = position;
            }
            if (!nodes[active_node].next.containsKey(active_edge())) {
                int leaf = newNode(position, oo);
                nodes[active_node].next.put(active_edge(), leaf);
                addSuffixLink(active_node); //rule 2
            } else {
                int next = nodes[active_node].next.get(active_edge());
                if (walkDown(next)) {
                    continue;   //observation 2
                }
                if (text[nodes[next].start + active_length] == c) { //observation 1
                    active_length++;
                    addSuffixLink(active_node); // observation 3
                    break;
                }
                int split = newNode(nodes[next].start, nodes[next].start + active_length);
                nodes[active_node].next.put(active_edge(), split);
                int leaf = newNode(position, oo);
                nodes[split].next.put(c, leaf);
                nodes[next].start += active_length;
                nodes[split].next.put(text[nodes[next].start], next);
                addSuffixLink(split); //rule 2
            }
            remainder--;

            if (active_node == root && active_length > 0) {  //rule 1
                active_length--;
                active_edge = position - remainder + 1;
            } else {
                active_node = nodes[active_node].link > 0 ? nodes[active_node].link : root; //rule 3
            }
        }
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
    
    void printTree(HashMap<Integer, String>map , int x, String current) {
        if (nodes[x].next.size() == 0) {
            map.put(x, current);
        } else {
            for (int child : nodes[x].next.values()) {
                printTree(map, child, current + nodes[child].toString());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SuffixTree st = new SuffixTree("to be or not to be that is the question");
        log.printf("%s", st.printTree());
    }
    
}
