package io.github.repir.tools.ByteRegex;

import io.github.repir.tools.ByteRegex.Node.TYPE;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Stack;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import io.github.repir.tools.Lib.PrintTools;

/**
 * A fast regular expression matcher for byte arrays and buffered byte streams.
 * The implementation is a simplified version of a Thompson's NFA, that can
 * parse characters, \ literal characters, \s \d \w \c as special space, digit,
 * word and letter characters (uppercases are the inverse), [^a-z] character
 * ranges, () groups, | choice, ? optional, + iteration and * optional
 * iteration, ^ matches start $ matches end-of-input, (?=xxx) for lookahead and
 * (?<=xxx) for lookbehind (only allowed whith fixed number of characters). By
 * default, \0 characters in the byte array are ignored. <p/>
 * finding a regex returns a (or an array of) pos object(s). Using the pos, the
 * caller can determine whether the regex was found, whether the match possibly
 * continues after end-of-input, the start and end positions.
 * <p/>
 * By default iterations are matched greedy, therefore \w.*\s will read until
 * the last whitespace found and always trigger endReached because if there is
 * more input in the stream a different result can be expected. For ungreedy matches
 * use *? and +?, e.g. \w.*?\s will only read till the first whitespace after
 * a \w characters.
 * <p/>
 * End-of-input are handled in a graceful manner. This way regular expressions
 * can be used on buffered streams. if an end-of-input is reached, this is
 * indicated in the returned pos object, along with the position at which a
 * potentially successful match started. If the input is a stream, the buffer
 * should be shifted towards the start of the potential match, filled with data
 * from the stream, and retried from that point. Typically, this is used for
 * large input files, using regular expressions that end with a repeated group.
 * matching " "* towards the end, the regular expression matcher cannot decide
 * whether the end of the match has been reached.
 * <p/>
 * Three things are different in this regex implementation than with standard
 * regular expressions: (1) \0 characters in the byte array are ignored for
 * matching (2) if \Q is added to the front of the expression, single and double
 * quotes are counted to make sure a match is not inside a quoted region. This
 * is added for matching text in or around HTML tags. (3) matching is case
 * insensitive by default, but can be turned to case sensitive by starting the
 * regular expression with \C.
 * <p/>
 * @author jeroen
 */
public class ByteRegex {

   public static Log log = new Log(ByteRegex.class);
   public Node root;
   public boolean quotesafe;
   public boolean casesensitive;
   public boolean inSingle = false;
   public boolean inDouble = false;
   public String pattern;
   ArrayList<Node> nodes = new ArrayList<Node>();

   protected ByteRegex() {
   }

   public ByteRegex(String pattern) {
      this.pattern = pattern;
      ANTLRInputStream input = new ANTLRInputStream(pattern);
      ByteRegexLexer lexer = new ByteRegexLexer(input);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      ByteRegexParser parser = new ByteRegexParser(tokens);
      try {
         parser.byteregex = this;
         parser.startRule();
         if (root != null) {
            root.setAllowed(nodes, 0);
         }
      } catch (RecognitionException ex) {
         log.fatalexception(ex, "ByteRegex cannot parse: %s", pattern);
      }
   }
   
   /**
    * Construct a ByteRegex as a choice between other ByteRegex. When matching
    * pos.pattern will indicate which pattern from the list matched (starting
    * from 0).
    * <p/>
    * @param list
    */
   public ByteRegex(ByteRegex... list) {
      root = new Node(TYPE.CHOICE, false);
      root.next = new Node[list.length];
      for (int i = 0; i < list.length; i++) {
         root.next[i] = list[i].root;
      }
      root.id = 0;
      root.clearAllowed();
      root.setAllowed(nodes, 0);
   }

   /**
    * @return a shallow copy of the regex. use clone() on the root element to
    * create a deep copy.
    */
   @Override
   public ByteRegex clone() {
      ByteRegex c = new ByteRegex();
      c.pattern = pattern;
      c.root = root;
      return c;
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

   public boolean isEmpty() {
      return root == null;
   }

   /**
    * @param b byte array to search for a match
    * @param start position to start matching
    * @param end matches cannot pass the end marker
    * @return pos.found()=true if a match is found at the start position and
    * ends before the end position
    */
   public Pos findFirst(byte b[], int start, int end) {
      return checkMatch(root, b, start, start, end);
   }

   /**
    * @param b byte array to search for a match
    * @param start position to start matching
    * @param end matches cannot pass the end marker
    * @return pos.found()=true if a match is found that starts and ends between
    * the start and end position
    */
   public Pos find(byte b[], int start, int end) {
      Pos pos = new Pos();
      inSingle = false;
      inDouble = false;
      if (root == null) {
         pos.start = start;
         pos.end = start;
      } else if (root.type == TYPE.START) {
         return findFirst(b, start, end);
      } else {
         int nextcheckquotes = start;
         for (int i = start; i <= end; i++) {
            if ((i == end || (root.allowed[b[i] & 0xFF])) && (!quotesafe || !(inSingle || inDouble))) {
               pos = checkMatch(root, b, start, i, end);
               if (pos.found() || pos.endreached) {
                  return pos;
               }
            }
            if (quotesafe && i < end) {
               if (nextcheckquotes == i) {
                  nextcheckquotes = i + 1;
                  switch (b[i]) {
                     case '\\':
                        nextcheckquotes = i + 2;
                        break;
                     case '"':
                        if (!inSingle) {
                           inDouble = !inDouble;
                        }
                        break;
                     case '\'':
                        if (!inDouble) {
                           inSingle = !inSingle;
                        }
                        break;
                  }
               }
            }
         }
         pos.start = end;
         pos.endreached = true;
      }
      return pos;
   }

   /**
    * wrapper for {@link #find(byte[], int, int) } to match regex in a string.
    */
   public Pos find(String str) {
      byte b[] = str.getBytes();
      return find(b, 0, b.length);
   }

   /**
    * wrapper for {@link #findFirst(byte[], int, int) } to match regex in a
    * string.
    */
   public Pos findFirst(String str) {
      byte b[] = str.getBytes();
      return findFirst(b, 0, b.length);
   }

   /**
    * wrapper for {@link #find(byte[], int, int) } to match regex in a string.
    * <p/>
    * @return true if match is found (at any position)
    */
   public boolean match(String str) {
      return find(str).found();
   }

   /**
    * wrapper for {@link #findFirst(byte[], int, int) } to match regex in a
    * string.
    * <p/>
    * @return true if match is found at start
    */
   public boolean matchFirst(String str) {
      return findFirst(str).found();
   }

   /**
    * wrapper for {@link #find(byte[], int, int) } to match regex in a string.
    * <p/>
    * @return position of the start of a match, or -1 if not found
    */
   public int matchPos(String str) {
      Pos find = find(str);
      return find.found() ? find(str).start : -1;
   }

   private int advance(byte b[], int currentpos, int end) {
      for (currentpos++; currentpos < end && b[currentpos] == 0; currentpos++);
      return currentpos;
   }

   private Pos checkMatch(Node root, byte b[], int start, int currentpos, int end) {
      Pos pos = new Pos();
      int choice = 0;
      Node currentnode = root;;
      ArrayDeque<State> s = new ArrayDeque<State>();
      currentpos = advance(b, currentpos - 1, end);
      pos.start = currentpos;
      pos.endreached = currentpos == end;
      //log.info("checkMatch( %s, %d, %d ) byte %d allowed %b ", root.toString(), currentpos, end, b[currentpos], root.allowed[ b[currentpos]]);
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
               pos.endreached = true;
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
                  //log.info("push %s choice %d pos %d", currentnode.toString(), choice, currentpos);
                  s.push(state);
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
                  //log.info("lookbehind %d", currentpos);
                  int newpos = currentpos;
                  int stepback = currentnode.lookbehindpos;
                  for (; newpos > start && stepback > 0; newpos--) {
                     if (b[newpos - 1] != 0) {
                        stepback--;
                     }
                  }
                  if (stepback == 0) {
                     Pos p = checkMatch(currentnode.next[0], b, start, newpos, end);
                     //log.info("lookbehind %b startpos %d endpos %d currentpos %d", p.found(), p.start, p.end, currentpos);
                     if (p.found()) {
                        State state = new State(currentnode, currentpos, choice);
                        s.push(state);
                        currentnode = currentnode.next[1];
                        choice = 0;
                        break;
                     }
                  }
               } else if (currentnode.next[choice].type == TYPE.END || currentnode.next[choice].type == TYPE.START) {
                  State state = new State(currentnode, currentpos, choice);
                  //log.info("push %s choice %d pos %d", currentnode.toString(), choice, currentpos);
                  s.push(state);
                  currentnode = currentnode.next[ choice];
                  choice = 0;
                  break;
               } else if (currentnode.type != TYPE.LOOKAHEAD) {
                  State state = new State(currentnode, currentpos, choice);
                  //log.info("push %s choice %d pos %d", currentnode.toString(), choice, currentpos);
                  s.push(state);
                  currentnode = currentnode.next[ choice];
                  choice = 0;
                  //goback = false;
                  break;
               } else if (currentpos == end) {
                  pos.endreached = true;
               } else if (currentnode.next[choice].allowed[ b[currentpos] & 0xFF] && currentnode.type == TYPE.LOOKAHEAD) {
                  //log.info("lookahead %d %s", currentpos, currentnode.next[0]);
                  Pos p = checkMatch(currentnode.next[0], b, start, currentpos, end);
                  //log.crash("lookahead return pos %d found %b", currentpos, p.found());
                  if (p.endreached) {
                     pos.endreached = true;
                     break;
                  } else if (p.found()) {
                     State state = new State(currentnode, currentpos, choice);
                     s.push(state);
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
            //log.info("back %s %s choice %d pos %d", currentnode.type.toString(), currentnode.toString(), choice, currentpos);
         }
         while (currentnode != null && choice >= currentnode.next.length) {
            if (s.size() > 0) {
               State state = s.pop();
               currentnode = state.node;
               choice = state.choice + 1;
               currentpos = state.pos;
               if (currentnode == root) {
                  pos.pattern = choice;
               }
               //log.info("pop %s choice %d pos %d", currentnode.toString(), choice, currentpos);
            } else {
               return pos;
            }
         }
      }
      if (currentnode == null) {
         pos.end = currentpos;
         //log.info("found pos %d", currentpos);
         return pos;
      }

      //log.info("done %d", currentpos);
      return pos;
   }

   /**
    * @return all matching positions, allowing overlap, so "\w+" used on "word"
    * will result in matches "word", "ord", "rd" and "d".
    */
   public ArrayList<Pos> findAllOverlap(byte b[], int start, int end) {
      ArrayList<Pos> list = new ArrayList<Pos>();
      while (start < end) {
         Pos p = find(b, start, end);
         //log.info("findall pos %d found %b", p.start, p.found());
         if (p.found()) {
            list.add(p);
            start = p.start + 1;
         } else {
            break;
         }
      }
      return list;
   }

   /**
    * @return all matching positions, without overlap, so "\w+" used on "word"
    * will return 1 match.
    */
   public ArrayList<Pos> findAll(byte b[], int start, int end) {
      ArrayList<Pos> list = new ArrayList<Pos>();
      while (start <= end) {
         Pos p = find(b, start, end);
         //log.info("findall pos %d found %b", p.start, p.found());
         if (p.found()) {
            list.add(p);
            start = Math.max(start + 1, p.end);
         } else {
            break;
         }
      }
      return list;
   }

   /**
    * @return all matching positions, without overlap, so "\w+" used on "word"
    * will return 1 match.
    */
   public ArrayList<Pos> findAll(String s) {
      byte b[] = s.getBytes();
      return findAll( b, 0, b.length );
   }
   
   /**
    * @return all matching positions, without overlap, so "\w+" used on "word"
    * will return 1 match.
    */
   public ArrayList<Pos> findAllStr(String s) {
      byte b[] = s.getBytes();
      return findAll( b, 0, b.length );
   }
   
   public static class Pos {

      public int start = -1;
      public int end = -1;
      
      // indicates end of input was reached and possibly reading more input could 
      // lead to a different match, if the caller has more input (e..g reading from
      // a stream) it should provide it and run match again.
      public boolean endreached;
      public int pattern;

      public Pos() {}
      
      public Pos(int start) {
         this.start = start;
      }
      
      public Pos(int start, int end) {
         this.start = start;
         this.end = end;
      }
      
      public Pos(int start, int end, boolean endreached) {
         this(start, end);
         this.endreached = endreached;
      }
      
      public static Pos notFound() {
         return new Pos(0,-1);
      }
      
      public static Pos endReached() {
         return new Pos(0,-1,true);
      }
      
      @Override
      public boolean equals(Object pos) {
         Pos p = (Pos)pos;
         if (end == -1 && p.end == -1)
            return true;
         return start == p.start && end == p.end && endreached == p.endreached;
      }
      
      public boolean found() {
         return end > -1;
      }
      
      public String toString( byte b[] ) {
         return new String( b, start, end - start );
      }
      
      public String toString( ) {
         return PrintTools.sprintf("Pos(%d,%d,%b,%d)", start,end,endreached,pattern);
      }
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
   }
}
