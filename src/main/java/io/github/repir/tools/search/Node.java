package io.github.repir.tools.search;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Helper class for ByteRegex. An expression is parsed into a tree of Nodes
 * which represent a single character, string, choice, optional or possible
 * repeated group.
 * <p/>
 * @author jeroen
 */
public class Node {

   public static Log log = new Log(Node.class);
   boolean casesensitive;
   int id = -1;
   int orpattern;
   int lookbehindpos;
   char allowedchar;
   public TYPE type;
   public Node[] next = new Node[0];
   public boolean[] allowed = new boolean[256];
   ArrayList<Node> pushallowed = new ArrayList<Node>();

   public enum TYPE {

      START,
      CHAR,
      CHOICE,
      LOOKAHEAD,
      LOOKBEHIND,
      END
   }

   public Node(TYPE type, boolean casesensitive) {
      this.type = type;
      this.casesensitive = casesensitive;
   }

   public Node clone(Node[] nodes) {
      Node n = new Node(type, casesensitive);
      System.arraycopy(allowed, 0, n.allowed, 0, allowed.length);
      n.id = id;
      nodes[id] = n;
      n.next = new Node[next.length];
      for (int i = 0; i < next.length; i++) {
         if (next[i] != null) {
            if (nodes[next[i].id] != null) {
               n.next[i] = nodes[next[i].id];
            } else {
               n.next[i] = next[i].clone(nodes);
            }
         }
      }
      return n;
   }

   public void setAllowed(char c) {
      allowedchar = c;
      allowed[c] = true;
      if (!casesensitive) {
         if (c >= 'a' && c <= 'z') {
            allowed[c - 32] = true;
         } else if (c >= 'A' && c <= 'Z') {
            allowed[c + 32] = true;
         }
      }
   }

   public void setAllowedSet(char... c) {
      allowedchar = Character.MAX_VALUE;
      for (char i : c) {
         setAllowed(i);
      }
   }

   public void combineAllowed(Node o) {
      //log.info("combineAllowed\n%s\n%s", this, o);
      for (int i = 0; i < allowed.length; i++) {
         allowed[i] |= o.allowed[i];
      }
   }

   public void setAllowedRange(char c, char d) {
      allowedchar = Character.MAX_VALUE;
      for (char i = c; i <= d; i++) {
         setAllowed(i);
      }
   }

   public void invertAllowed() {
      for (int i = 0; i < allowed.length; i++) {
         allowed[i] = !allowed[i];
      }
   }

   public char allowedChar() {
      return allowedchar;
   }

   public void clearAllowed() {
      if (this.id >= 0) {
         id = -1;
         for (int i = 0; i < next.length; i++) {
            if (next[i] != null) {
               next[i].clearAllowed();
            }
         }
      }
   }

   static ArrayList<Integer> allbits = setAll();;
   
   private static ArrayList<Integer> setAll() {
      ArrayList<Integer> list = new ArrayList<Integer>();
      for (int i = 0; i < 256; i++)
         list.add(i);
      return list;
   }
   
   public int setAllowed(ArrayList<Node> list, int id) {
      if (this.id < 0) {
         this.id = id++;
         list.add(this);
         for (int i = 0; i < next.length; i++) {
            if (next[i] != null) {
               id = next[i].setAllowed(list, id);
            }
         }
         if (type != TYPE.CHAR && type != TYPE.LOOKAHEAD) { // set allowed true if possible last node
            for (int choice = 0; choice < next.length; choice++) {
               if (next[choice] == null) {
                  setAllowedRec(allbits, new ArrayList<Node>());
               }
            }
         }
         if (type == TYPE.LOOKAHEAD) {
            setAllowedDependency(next[0]);
         } else if (type == TYPE.LOOKBEHIND) {
            setAllowedDependency(next[1]);
            Node n = next[0];
            int steps = 0;
            for (; n != null && n != next[1]; n = n.next[0]) {
               if (n.type == TYPE.CHAR) {
                  steps++;
               } else if (n.type != TYPE.START) {
                  log.fatal("can only use look behind with characters");
               }
            }
            lookbehindpos = steps;
         } else if (type != TYPE.CHAR) {
            for (Node n : next) {
               setAllowedDependency(n);
            }
         }
      }
      return id;
   }

   public void setAllowedRec(ArrayList<Integer> newallowed, ArrayList<Node> past) {
      for (int i : newallowed)
         allowed[i] = true;
      past.add(this);
      for (Node n : pushallowed) {
         if (!past.contains(n))
            n.setAllowedRec(newallowed, past);
      }
      past.remove(this);
   }
   
   protected void setAllowedDependency(Node next) {
      if (next != null) {
         ArrayList<Integer> newallowed = new ArrayList<Integer>();
         for (int i = 0; i < 256; i++) {
            if (next.allowed[i] && !allowed[i]) {
               newallowed.add(i);
            }
         }
         setAllowedRec(newallowed, new ArrayList<Node>());
         next.pushallowed.add(this);
      }
   }

   @Override
   public String toString() {
      if (type == TYPE.CHAR) {
         StringBuilder sb = new StringBuilder();
         sb.append("CHAR '");
         for (int i = 13; i < 256; i++) {
            if (allowed[i]) {
               if (i < 33) {
                  sb.append("\\").append(i);
               } else {
                  sb.append((char) i);
               }
            }
         }
         return sb.append('\'').toString();
      } else {
         return "Node [" + id + "] " + type + " " + next.length;
      }
   }

   public void print(int lvl) {
      StringBuilder sb = new StringBuilder();
      for (int i = 13; i < 256; i++) {
         if (allowed[i]) {
            if (i < 33) {
               sb.append("\\").append(i);
            } else {
               sb.append((char) i);
            }
         }
      }
      if (type == TYPE.CHAR) {
         log.printf("%sCHAR '%s'", level(this, lvl), sb.toString());
         for (Node n : next) {
            printNext(n, lvl, lvl);
         }
      } else if (type == TYPE.LOOKAHEAD) {
         log.printf("%sNode %s '%s' [", level(this, lvl), type.toString(), sb);
         printNext(next[0], lvl, lvl + 2);
         log.printf("%" + lvl + "s]", "");
         printNext(next[1], lvl, lvl);
      } else if (type == TYPE.LOOKBEHIND) {
         log.printf("%sNode %s '%s' [", level(this, lvl), type.toString(), sb);
         printNext(next[0], lvl, lvl + 2);
         log.printf("%" + lvl + "s]", "");
         printNext(next[1], lvl, lvl);
      } else {
         log.printf("%sNode %s '%s' [", level(this, lvl), type.toString(), sb);
         boolean first = true;
         for (int i = 0; i < next.length; i++) {
            Node n = next[i];
            if (first) {
               first = false;
            } else {
               log.printf("%" + lvl + "s|", "");
            }
            log.printf("(%d)", i);
            printNext(n, lvl, lvl + 2);
         }
         log.printf("%" + lvl + "s]", "");
      }
   }

   public String level(Node n, int level) {
      return PrintTools.sprintf("[%d]%" + level + "s", n.id, "");
   }

   public void printNext(Node n, int lvl, int newlvl) {
      if (n != null) {
         if (n.id > id) {
            n.print(newlvl);
         } else {
            log.printf("%" + lvl + "s<--", "");
         }
      }
   }
}
