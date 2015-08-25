package io.github.htools.search;

import io.github.htools.search.Node.TYPE;
import io.github.htools.lib.Log;
import java.util.ArrayDeque;
import java.util.ArrayList;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import io.github.htools.lib.PrintTools;

/**
 * A fast regular expression matcher for byte arrays and buffered byte streams.
 * The implementation is a simplified version of a Thompson's NFA, that can
 * parse characters, \ literal characters, \s \d \w \c as whitespace, digit,
 * word and letter characters (uppercases are the inverse), [^a-z] character
 * ranges, () groups, | choice, ? optional, + iteration and * optional
 * iteration, ^ matches start $ matches end-of-input, (?=xxx) for lookahead and
 * (?&lt;=xxx) for lookbehind (only allowed whith fixed number of characters). By
 * default, \0 characters in the byte array are ignored. 
 * <p>
 * finding a regex returns a (or an array of) pos object(s). Using the pos, the
 * caller can determine whether the regex was found, whether the exists possibly
 * continues after end-of-input, the start and end positions.
 * <p>
 * By default iterations are matched greedy, therefore \w.*\s will read until
 * the last whitespace found and always trigger endReached because if there is
 * more input in the stream a different result can be expected. For ungreedy
 * matches use *? and +?, e.g. \w.*?\s will only read till the first whitespace
 * after a \w characters.
 * <p>
 * End-of-input are handled in a graceful manner. This way regular expressions
 * can be used on buffered streams. if an end-of-input is reached, this is
 * indicated in the returned pos object, along with the position at which a
 * potentially successful exists started. If the input is a stream, the buffer
 * should be shifted towards the start of the potential exists, filled with data
 * from the stream, and retried from that point. Typically, this is used for
 * large input files, using regular expressions that end with a repeated group.
 * matching " "* towards the end, the regular expression matcher cannot decide
 * whether the end of the exists has been reached.
 * <p>
 * Three things are different in this regex implementation than with standard
 * regular expressions: (1) \0 characters in the byte array are ignored for
 * matching. (2) \Q is replaced with an escape safe quoted sequence, e.g. '"',
 * "'", "\"", '\'', this was added for matching text in or around HTML and XML
 * tags. (3) matching is case insensitive by default, but can be turned to case
 * sensitive by starting the regular expression with \C.
 * <p>
 * @author jeroen
 */
public class ByteRegex extends ByteSearch {

   public static Log log = new Log(ByteRegex.class);
   public Node root;
   public String pattern;
   ArrayList<Node> nodes = new ArrayList<Node>();

   protected ByteRegex() {
   }

   public ByteRegex(String pattern) {
      this.pattern = convertPattern(pattern);
      root = parseRegexI(this.pattern, nodes);
   }

   public ByteRegex(String pattern, Node root) {
      this.pattern = pattern;
      this.root = root;
   }

   public static Node parseRegex(String pattern, ArrayList<Node> list) {
      return parseRegexI(convertPattern(pattern), list);
   }

   private static Node parseRegexI(String pattern, ArrayList<Node> list) {
      ANTLRInputStream input = new ANTLRInputStream(pattern);
      ByteRegexLexer lexer = new ByteRegexLexer(input);
      lexer.removeErrorListeners();
      lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      ByteRegexParser parser = new ByteRegexParser(tokens);
      parser.removeErrorListeners();
      parser.addErrorListener(DescriptiveErrorListener.INSTANCE);
      try {
         parser.startRule();
         Node root = parser.root;
         if (root != null) {
            root.setAllowed(list, 0);
            for (Node n : list) {
               n.pushallowed = new ArrayList<Node>();
            }
         }
         return root;
      } catch (RecognitionException ex) {
         log.fatalexception(ex, "ByteRegex cannot parse: %s", pattern);
      }
      return null;
   }

   public static Node parseRegex(String pattern) {
      return parseRegex(pattern, new ArrayList<Node>());
   }

   private ByteRegex(ByteRegex... list) {
      root = new Node(TYPE.CHOICE, false);
      root.next = new Node[list.length];
      for (int i = 0; i < list.length; i++) {
         root.next[i] = list[i].root;
         if (i == 0)
            pattern = list[i].pattern;
         else
            pattern += " && " + list[i].pattern;
      }
      root.id = 0;
      root.clearAllowed();
      root.setAllowed(nodes, 0);
      for (Node n : nodes) {
         n.pushallowed = new ArrayList<Node>();
      }
   }
   
   /**
    * Construct a ByteRegex as a choice between other ByteRegex. When matching
    * pos.pattern will indicate which pattern from the list matched (starting
    * from 0).
    * <p>
    * @param list
    */
   public static ByteRegex combine(ByteRegex... list) {
      return new ByteRegex(list);
   }

   public static ByteRegex create(String pattern) {
      return new ByteRegex(pattern);
   }

   /**
    * @return a shallow copy of the regex. use clone() on the root element to
    * create a deep copy.
    */
   public ByteRegex clone() {
      ByteRegex c = new ByteRegex();
      c.pattern = pattern;
      c.root = root;
      return c;
   }

   /**
    * replace \Q in pattern with a quoted string that is escape safe
    */
   protected static String convertPattern(String pattern) {
      return pattern.replace("\\Q", "((\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\")|('([^'\\\\]*(\\\\.[^'\\\\]*)*)'))");
   }

   /**
    * @return a new regex the surrounds this regex with a lookahead, which is
    * useful for matching without consuming the input. The input regex is used
    * as a shallow copy within the lookahead.
    */
   public ByteRegex lookAhead() {
      ByteRegex c = clone();
      c.root = root.clone(new Node[nodes.size()]);
      if (root.type != TYPE.LOOKAHEAD) {
         Node lookahead = new Node(TYPE.LOOKAHEAD, root.casesensitive);
         lookahead.next = new Node[2];
         lookahead.next[0] = c.root;
         lookahead.next[1] = null;
         System.arraycopy(root.allowed, 0, lookahead.allowed, 0, root.allowed.length);
         c.root = lookahead;
         c.pattern = "(?=" + pattern + ")";
      }
      return c;
   }

   /**
    * print the regex, or at least attempts to.
    */
   public void print() {
      log.printf("Regex %s", pattern);
      if (root != null) {
         root.print(1);
      }
   }

   @Override
   public String toString() {
      return "ByteRegex(" + pattern + ")";
   }

   public boolean isEmpty() {
      return root == null;
   }

   /**
    * @param b byte array to search for a exists
    * @param start position to start matching
    * @param end matches cannot pass the end marker
    * @return pos.found()=true if a exists is found at the start position and
    * ends before the end position
    */
   @Override
   public ByteSearchPosition matchPos(byte b[], int start, int end) {
      return getPos(root, b, start, start, end);
   }

   @Override
   public int matchEnd(byte b[], int start, int end) {
      ByteSearchPosition p = getPos(root, b, start, start, end);
      return p.found() ? p.end : Integer.MIN_VALUE;
   }

   @Override
   public boolean match(byte b[], int start, int end) {
      return getPos(root, b, start, start, end).found();
   }

   @Override
   public int findNoQuoteSafe(byte b[], int start, int end) {
      ByteSearchPosition p = findPos(b, start, end);
      return p.found() ? p.start : Integer.MIN_VALUE;
   }

   @Override
   public int findEndNoQuoteSafe(byte b[], int start, int end) {
      ByteSearchPosition p = findPos(b, start, end);
      return p.found() ? p.end : Integer.MIN_VALUE;
   }

   /**
    * @param b byte array to search for a exists
    * @param start position to start matching
    * @param end matches cannot pass the end marker
    * @return pos.found()=true if a exists is found that starts and ends between
    * the start and end position
    */
   @Override
   public ByteSearchPosition findPosNoQuoteSafe(byte b[], int start, int end) {
      ByteSearchPosition pos = new ByteSearchPosition(b);
      if (root == null) {
         pos.start = start;
         pos.end = start;
      } else if (root.type == TYPE.START) {
         return matchPos(b, start, end);
      } else {
         for (int i = start; i <= end; i++) {
            if (i == end || root.allowed[b[i] & 0xFF]) {
               pos = getPos(root, b, start, i, end);
               if (pos.found() || pos.endreached) {
                  return pos;
               }
            }
         }
         pos.start = end;
         pos.endreached = true;
      }
      return pos;
   }

   @Override
   public int findQuoteSafe(byte b[], int start, int end) {
      return findPosQuoteSafe(b, start, end).start;
   }

   @Override
   public int findEndQuoteSafe(byte b[], int start, int end) {
      return findPosQuoteSafe(b, start, end).end;
   }

   /**
    * Quote safe version of find, that avoids finding matches within a quoted
    * value that can contain escape characters. E.g. findQuoteSafe with pattern
    * "s" will only exists the last occurrence in "s" "\"s" 's' '\'s' '"s' s.
    */
   @Override
   public ByteSearchPosition findPosQuoteSafe(byte b[], int start, int end) {
      ByteSearchPosition pos = new ByteSearchPosition(b);
      if (root == null) {
         pos.start = start;
         pos.end = start;
      } else if (root.type == TYPE.START) {
         return matchPos(b, start, end);
      } else {
         LOOP:
         for (int i = start; i <= end; i++) {
            if (i < end) {
               switch (b[i]) {
                  case '"':
                     for (i++; i < end; i++) {
                        if (b[i] == '\\') {
                           i++;
                        } else if (b[i] == '"') {
                           continue LOOP;
                        }
                     }
                     break LOOP;
                  case '\'':
                     for (i++; i < end; i++) {
                        if (b[i] == '\\') {
                           i++;
                        } else if (b[i] == '\'') {
                           continue LOOP;
                        }
                     }
                     break LOOP;
               }
            }
            if (i == end || root.allowed[b[i] & 0xFF]) {
               pos = getPos(root, b, start, i, end);
               if (pos.found() || pos.endreached) {
                  return pos;
               }
            }
         }
         pos.start = end;
         pos.endreached = true;
      }
      return pos;
   }

   /**
    * Quote safe version of find, that avoids finding matches within a quoted
    * value that can contain escape characters. E.g. findQuoteSafe with pattern
    * "s" will only exists the last occurrence in "s" "\"s" 's' '\'s' '"s' s.
    */
   @Override
   public ByteSearchPosition findPosDoubleQuoteSafe(byte b[], int start, int end) {
      ByteSearchPosition pos = new ByteSearchPosition(b);
      if (root == null) {
         pos.start = start;
         pos.end = start;
      } else if (root.type == TYPE.START) {
         return matchPos(b, start, end);
      } else {
         LOOP:
         for (int i = start; i <= end; i++) {
            if (i < end) {
               switch (b[i]) {
                  case '"':
                     for (i++; i < end; i++) {
                        if (b[i] == '\\') {
                           i++;
                        } else if (b[i] == '"') {
                           continue LOOP;
                        }
                     }
                     break LOOP;
               }
            }
            if (i == end || root.allowed[b[i] & 0xFF]) {
               pos = getPos(root, b, start, i, end);
               if (pos.found() || pos.endreached) {
                  return pos;
               }
            }
         }
         pos.start = end;
         pos.endreached = true;
      }
      return pos;
   }

   protected int advance(byte b[], int currentpos, int end) {
      for (currentpos++; currentpos < end && b[currentpos] == 0; currentpos++);
      return currentpos;
   }

   protected ByteSearchPosition getPos(Node root, byte b[], int start, int currentpos, int end) {
      //print();
      ByteSearchPosition pos = new ByteSearchPosition(b);
      int choice = 0;
      Node currentnode = root;
      ArrayDeque<State> states = new ArrayDeque<State>();
      currentpos = advance(b, currentpos - 1, end);
      pos.start = currentpos;
      pos.endreached = currentpos == end;
      while (currentnode != null) {
         if (currentnode.type == TYPE.START) {
            if (currentpos == start) {
               currentnode = currentnode.next[0];
            } else {
               choice = 1;
            }
         } else if (currentnode.type == TYPE.END) {
            if (currentpos == end) {
               currentnode = currentnode.next[0];
            } else {
               choice = 1;
            }
         } else if (currentnode.type == TYPE.CHAR) {
            if (currentpos < end && currentnode.allowed[b[currentpos] & 0xFF]) {
               currentpos = advance(b, currentpos, end);
               if (currentnode.next[0] == null) {
                  currentnode = null;
                  break;
               } else if (currentpos == end || currentnode.next[0].allowed[ b[currentpos] & 0xFF]) {
                  if (currentnode.next[0].type != TYPE.END && currentnode.next[0].type != TYPE.START) {
                     pos.endreached |= currentpos == end;
                  }
                  State state = new State(currentnode, currentpos, choice);
                  states.push(state);
                  currentnode = currentnode.next[ choice];
                  choice = 0;
               } else {
                  choice = 1;
               }
            } else {
               choice = 1;
            }
         } else {
            while (choice < currentnode.next.length) {
               if (currentnode.next[choice] == null) {
                  currentnode = null;
                  break;
               } else if (currentnode.type == TYPE.LOOKBEHIND) {
                  //log.info("lookbehind %d %d", currentpos, currentnode.lookbehindpos);
                  int newpos = currentpos;
                  int stepback = currentnode.lookbehindpos;
                  for (; newpos > start && stepback > 0; newpos--) {
                     if (b[newpos - 1] != 0) {
                        stepback--;
                     }
                  }
                  if (stepback == 0) {
                     ByteSearchPosition p = getPos(currentnode.next[0], b, start, newpos, end);
                     //log.info("lookbehind %b startpos %d endpos %d currentpos %d", p.found(), p.start, p.end, currentpos);
                     if (p.found()) {
                        State state = new State(currentnode, currentpos, choice);
                        //log.info("push lookbehind %d %s", states.size(), state);
                        states.push(state);
                        currentnode = currentnode.next[1];
                        choice = 0;
                        break;
                     }
                  }
               } else if (currentnode.next[choice].type == TYPE.END || currentnode.next[choice].type == TYPE.START) {
                  State state = new State(currentnode, currentpos, choice);
                  //log.info("push end %d %s", states.size(), state);
                  states.push(state);
                  currentnode = currentnode.next[ choice];
                  choice = 0;
                  break;
               } else if (currentnode.type != TYPE.LOOKAHEAD) {
                  State state = new State(currentnode, currentpos, choice);
                  //log.info("push pos %d oldstatenum %d newstate %s", currentpos, states.size(), state);
                  states.push(state);
                  currentnode = currentnode.next[ choice];
                  choice = 0;
                  break;
               } else if (currentpos == end) {
                  pos.endreached = true;
               } else if (currentnode.next[choice].allowed[ b[currentpos] & 0xFF] && currentnode.type == TYPE.LOOKAHEAD) {
                  //log.info("lookahead %d %s", currentpos, currentnode.next[0]);

                  ByteSearchPosition p = getPos(currentnode.next[0], b, start, currentpos, end);
                  if (p.endreached) {
                     pos.endreached = true;
                     break;
                  } else if (p.found()) {
                     State state = new State(currentnode, currentpos, choice);
                     //log.info("push lookahead %d %s", states.size(), state);
                     states.push(state);
                     currentnode = currentnode.next[1];
                     choice = 0;
                     //goback = false;
                     //log.info("pos %d %d %b %s", p.start, p.end, p.found(), currentnode);
                     break;
                  }
               }
               if (currentnode.type == TYPE.CHOICE) {
                  choice++;
                  //log.info("choice %d", choice);
                  if (currentnode == root) {
                     pos.pattern = choice;
                  }
               } else {
                  choice = currentnode.next.length;
               }
            }
         }
         if (currentnode != null) {
            //log.info("back node %s choice %d pos %d", currentnode.id, choice, currentpos);
         }
         while (currentnode != null && choice >= currentnode.next.length) {
            if (states.size() > 0) {
               State state = states.pop();
               currentnode = state.node;
               choice = state.choice + 1;
               currentpos = state.pos;
               if (currentnode == root) {
                  pos.pattern = choice;
               }
            } else {
               return pos;
            }
         }
      }
      if (currentnode == null) {
         pos.end = currentpos;
         return pos;
      }

      //log.info("done %d", currentpos);
      return pos;
   }

   public class State {

      public Node node;
      public int pos;
      public int choice = -1;

      public State(Node n, int pos, int choice) {
         this.node = n;
         this.pos = pos;
         this.choice = choice;
      }

      @Override
      public String toString() {
         return PrintTools.sprintf("State(pos=%d,choice=%d,node=%d)", pos, choice, node.id);
      }
   }
}
