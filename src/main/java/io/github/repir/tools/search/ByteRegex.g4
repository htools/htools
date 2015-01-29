grammar ByteRegex;

@header {
    package io.github.repir.tools.search;
    import io.github.repir.tools.search.Node.*; 
    import java.util.ArrayList;
    import io.github.repir.tools.Lib.Log;
}

@members {
   public static Log log = new Log( ByteRegexParser.class );
   private boolean casesensitive = false;
   public Node root;
}

/** 

@author Jeroen
*/

startRule
   : (QUES)? 
     (CASE { casesensitive = true; } )?
     (frag { root = $frag.fragout.start;
               //log.info("startRule %s %s %d", $frag.fragout, $frag.fragout.start.type, $frag.fragout.end.size() ); 
            })? EOF
;

frag returns [ Fragment fragout ] 
   @after { 
            //log.info("frag %s %s %d", $fragout, ($fragout).start.type, ($fragout).end.size() ); 
          }
   :
   string { $fragout = $string.fragout; }
   ( OR string {
      if ( $fragout.start.type == TYPE.CHOICE ) {
        Node next[] = new Node[ $fragout.start.next.length + 1 ];
        System.arraycopy( $fragout.start.next, 0, next, 0, $fragout.start.next.length );
        next[ $fragout.start.next.length ] = $string.fragout.start;
        $fragout.start.next = next;
      } else {
        Node node = new Node( TYPE.CHOICE, casesensitive );
        node.next = new Node[2];
        node.next[0] = $fragout.start;
        node.next[1] = $string.fragout.start;
        ($fragout).start = node;
        $fragout.addEnd( $string.fragout );
      } 
   } )*
;

string returns [ Fragment fragout ]
   @after { 
            //log.info("string %s %s %d", $fragout, ($fragout).start.type, ($fragout).end.size() ); 
          }
   : ( operand { 
             if ($fragout == null)
                $fragout = $operand.fragout;
             else {
                $fragout.setEnds( $operand.fragout.start );
                $fragout.addEnd( $operand.fragout ); 
             }
           }
     ) +
;

operand returns [ Fragment fragout ]
   @after { 
            //log.info("operand %s %s %d", $fragout, ($fragout).start.type, ($fragout).end.size() ); 
          }
   : set { $fragout = $set.fragout; }
      (( 
        QUES { 
           Node node = new Node( TYPE.CHOICE, casesensitive );
           node.next = new Node[2];
           node.next[0] = $fragout.start;
             } 
       ( QUES { node.next[1] = node.next[0]; node.next[0] = null; } )? 
           { $fragout.addEnd( node );
             ($fragout).start = node;
        } )
      | (STAR {
           Node node = new Node( TYPE.CHOICE, casesensitive );
           node.next = new Node[2];
           node.next[0] = $fragout.start;
             } 
       ( QUES { node.next[1] = node.next[0]; node.next[0] = null; } )? 
           {           $fragout.setEnds( node );
           ($fragout).start = node;
           $fragout.addEnd(node);
      })
      | (PLUS {
           Node node = new Node( TYPE.CHOICE, casesensitive );
           node.next = new Node[2];
           node.next[0] = $fragout.start;
             } 
       ( QUES { node.next[1] = node.next[0]; node.next[0] = null; } )? 
           {  $fragout.setEnds( node );
              $fragout.addEnd( node );
       })
     )? /* add braces fro repeated group */
;

set returns [ Fragment fragout ]
   @after { 
            //log.info("set %s %s %d", $fragout, ($fragout).start.type, ($fragout).end.size() ); 
          }
   : ( bracket { $fragout = new Fragment();
                 ($fragout).start = $bracket.node;
                 $fragout.addEnd( $bracket.node );
               }
   | BRACKOPEN ( 
          frag { $fragout = $frag.fragout; } 
       | ( LOOKAHEAD frag // lookahead 
            { 
            $fragout = $frag.fragout;
            Fragment f = new Fragment();
            Node lookahead = new Node( TYPE.LOOKAHEAD, casesensitive );
            lookahead.next = new Node[2];
            lookahead.next[0] = $fragout.start;
            lookahead.next[1] = null;
            $fragout.setEnds( null );
            $fragout.addEnd( lookahead );
            ($fragout).start = lookahead;
           }
         )
       | ( LOOKBEHIND frag // lookahead 
            { 
            $fragout = $frag.fragout;
            Fragment f = new Fragment();
            Node lookahead = new Node( TYPE.LOOKBEHIND, casesensitive );
            lookahead.next = new Node[2];
            lookahead.next[0] = $fragout.start;
            lookahead.next[1] = null;
            $fragout.setEnds( null );
            $fragout.addEnd( lookahead );
            ($fragout).start = lookahead;
           }
         )
     ) BRACKCLOSE )
;

bracket returns [ Node node ]
   @init {  boolean positive = true;
            char lastcharacter = 0;
   }
   @after { if (!positive)
               $node.invertAllowed();
             //log.info("bracket %s %s %d", $node, ($node).type, ($node).next.length ); 
          }
   : character { $node = $character.node; }
   | MINUS { $node = new Node( TYPE.CHAR, casesensitive );
             ($node).next = new Node[1];
             $node.setAllowed( '-' );
           }
   | ( BLOCKOPEN
     character { $node = new Node( TYPE.CHAR, casesensitive );
                 ($node).next = new Node[1];
                 if ($character.node.type == TYPE.START) {
                    positive=false;
                 } else {
                    lastcharacter = $character.node.allowedChar();
                    $node.combineAllowed( $character.node );
                 }
               }
     ( ( MINUS character { 
          char nextcharacter = $character.node.allowedChar();
          $node.setAllowedRange( (char)(lastcharacter + 1), nextcharacter);
          lastcharacter = nextcharacter;
       })
       | character { 
         //log.info("add bracket %s", $character.node.toString());
         lastcharacter = $character.node.allowedChar();
         $node.combineAllowed( $character.node );
      } )* BLOCKCLOSE )
;

character returns [ Node node ]
   @init { $node = new Node( TYPE.CHAR, casesensitive );
           ($node).next = new Node[1];
         }
   @after {  //log.info("character %s %s %d", $node, ($node).type, ($node).next.length ); 
         }
   :
   ( CHAR { $node.setAllowed( $CHAR.text.charAt(0) ); }
     | START { ($node).type = TYPE.START; }
     | END { ($node).type = TYPE.END; }
     | DOT { $node.setAllowedRange( '\0', '~'); }
     | LITERAL { switch ($LITERAL.text.charAt(1)) {
                   case 'g' :
                      $node.setAllowed( ' ' );
                      break;
                   case 'n' :
                      $node.setAllowedSet( '\r', '\n', (char)10 );
                      break;
                   case 't' :
                      $node.setAllowedSet( '\t' );
                      break;
                   case 's' :
                      $node.setAllowedSet( ' ', '\n', '\t', '\r' );
                      break;
                   case 'S' :
                      $node.setAllowedSet( ' ', '\n', '\t', '\r' );
                      $node.invertAllowed();
                      break;
                   case 'w' :
                      $node.setAllowedRange( 'A', 'Z' );
                      $node.setAllowedRange( 'a', 'z' );
                      $node.setAllowedRange( '0', '9' );
                      $node.setAllowed( '_' );
                      break;
                   case 'W' :
                      $node.setAllowedRange( 'A', 'Z' );
                      $node.setAllowedRange( 'a', 'z' );
                      $node.setAllowedRange( '0', '9' );
                      $node.setAllowed( '_' );
                      $node.invertAllowed();
                      break;
                   case 'c' :
                      $node.setAllowedRange( 'A', 'Z' );
                      $node.setAllowedRange( 'a', 'z' );
                      break;
                   case 'C' :
                      $node.setAllowedRange( 'A', 'Z' );
                      $node.setAllowedRange( 'a', 'z' );
                      $node.invertAllowed();
                      break;
                   case 'd' :
                      $node.setAllowedRange( '0', '9' );
                      break;
                   case 'D' :
                      $node.setAllowedRange( '0', '9' );
                      $node.invertAllowed( );
                      break;
                   default:
                      $node.setAllowed( $LITERAL.text.charAt(1) );
               } }
   ) 
;

OR   : '|';
LITERAL     : '\\' ~('C');
CASE        : '\\' 'C';
STAR        : '*';
QUES        : '?';
PLUS        : '+';
DOT         : '.';
END         : '$';
CHAR        : ~('*' | '?' | '+' | '(' | ')' | '{' | '}' | '[' | ']' | '^' | '-' | '.' | '$');
BRACKOPEN   : '(';
BRACKCLOSE  : ')';
BRACEOPEN   : '{';
BRACECLOSE  : '}';
BLOCKOPEN   : '[';
BLOCKCLOSE  : ']';
START    : '^';
MINUS       : '-';
LOOKAHEAD   : '?' '=';
LOOKBEHIND   : '?' '<' '=';





