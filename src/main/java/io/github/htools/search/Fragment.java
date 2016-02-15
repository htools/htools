package io.github.htools.search;

import io.github.htools.lib.Log;

import java.util.ArrayList;

import static io.github.htools.lib.PrintTools.sprintf;

public class Fragment {
   public static Log log = new Log(Fragment.class); 
   public Node start;
   public ArrayList<Node> end = new ArrayList<Node>();

   public void addEnd(Node n) {
      end.add(n);
   }

   public void addEnd(Fragment f) {
      for (Node n : f.end) {
         end.add(n);
      }
   }

   public void setEnds(Node n) {
      for (Node e : end) {
         for (int i = 0; i < e.next.length; i++) {
            if (e.next[i] == null) {
               e.next[i] = n;
               break;
            }
         }
      }
      end = new ArrayList<Node>();
   }
   
   public String toString() {
       return sprintf("Fragment start %s ends %d %s", start, end.size(), end);
   }
}
