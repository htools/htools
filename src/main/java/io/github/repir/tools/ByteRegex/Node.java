package io.github.repir.tools.ByteRegex;

import io.github.repir.tools.Lib.Log;
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
   TYPE type;
   public Node[] next = new Node[0];
   public boolean[] allowed = new boolean[256];

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

   public void combineAllowed( boolean otherallowed[] ) {
      for (int i = 0; i < allowed.length; i++)
         allowed[i] |= otherallowed[i];
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

   public int setAllowed(ArrayList<Node> list, int id) {
      if (this.id < 0) {
         this.id = id++;
         list.add(this);
         for (int i = 0; i < next.length; i++) {
            if (next[i] != null) {
               id = next[i].setAllowed(list, id);
            }
         }
         if (type != TYPE.CHAR && type != TYPE.LOOKAHEAD) {
            for (int choice = 0; choice < next.length; choice++)
               if (next[choice] == null)
                  Arrays.fill(allowed, true);
         }
         if (type == TYPE.LOOKAHEAD) {
            if (next[0] != null) {
               allowed = next[0].allowed;
            }
         } else if (type == TYPE.LOOKBEHIND) {
            if (next[1] != null)
               allowed = next[1].allowed;
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
               if (n != null) {
                  for (int i = 0; i < 256; i++) {
                     allowed[i] |= n.allowed[i];
                  }
               }
            }
         }
         //if (type == TYPE.LOOKAHEADRETURN && next[0] == null)
         //   Arrays.fill(allowed, true);
      }
      return id;
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
         return "Node " + type + " " + next.length;
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
         log.printf("%" + lvl + "sCHAR '%s'", "", sb.toString());
         for (Node n : next) {
            printNext(n, lvl, lvl);
         }
      } else if (type == TYPE.LOOKAHEAD) {
         log.printf("%" + lvl + "sNode %s '%s' [", "", type.toString(), sb);
         printNext(next[0], lvl, lvl + 2);
         log.printf("%" + lvl + "s]", "");
         printNext(next[1], lvl, lvl);
      } else if (type == TYPE.LOOKBEHIND) {
         log.printf("%" + lvl + "sNode %s '%s' [", "", type.toString(), sb);
         printNext(next[0], lvl, lvl + 2);
         log.printf("%" + lvl + "s]", "");
         printNext(next[1], lvl, lvl);
      } else {
         log.printf("%" + lvl + "sNode %s '%s' [", "", type.toString(), sb);
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
