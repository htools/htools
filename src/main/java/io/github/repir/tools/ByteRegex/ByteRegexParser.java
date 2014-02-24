// Generated from ByteRegex.g4 by ANTLR 4.1

    package io.github.repir.tools.ByteRegex;
    import io.github.repir.tools.ByteRegex.Node.*; 
    import java.util.ArrayList;
    import io.github.repir.tools.Lib.Log;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ByteRegexParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		OR=1, LITERAL=2, QUOTES=3, CASE=4, STAR=5, QUES=6, PLUS=7, DOT=8, END=9, 
		CHAR=10, BRACKOPEN=11, BRACKCLOSE=12, BLOCKOPEN=13, BLOCKCLOSE=14, START=15, 
		MINUS=16, LOOKAHEAD=17, LOOKBEHIND=18;
	public static final String[] tokenNames = {
		"<INVALID>", "'|'", "LITERAL", "QUOTES", "CASE", "'*'", "'?'", "'+'", 
		"'.'", "'$'", "CHAR", "'('", "')'", "'['", "']'", "'^'", "'-'", "LOOKAHEAD", 
		"LOOKBEHIND"
	};
	public static final int
		RULE_startRule = 0, RULE_frag = 1, RULE_string = 2, RULE_operand = 3, 
		RULE_set = 4, RULE_bracket = 5, RULE_character = 6;
	public static final String[] ruleNames = {
		"startRule", "frag", "string", "operand", "set", "bracket", "character"
	};

	@Override
	public String getGrammarFileName() { return "ByteRegex.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }


	   public static Log log = new Log( ByteRegexParser.class );
	   public ByteRegex byteregex;

	public ByteRegexParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StartRuleContext extends ParserRuleContext {
		public FragContext frag;
		public FragContext frag() {
			return getRuleContext(FragContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ByteRegexParser.EOF, 0); }
		public TerminalNode CASE() { return getToken(ByteRegexParser.CASE, 0); }
		public TerminalNode QUES() { return getToken(ByteRegexParser.QUES, 0); }
		public TerminalNode QUOTES() { return getToken(ByteRegexParser.QUOTES, 0); }
		public StartRuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_startRule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).enterStartRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).exitStartRule(this);
		}
	}

	public final StartRuleContext startRule() throws RecognitionException {
		StartRuleContext _localctx = new StartRuleContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_startRule);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(15);
			_la = _input.LA(1);
			if (_la==QUES) {
				{
				setState(14); match(QUES);
				}
			}

			setState(21);
			switch (_input.LA(1)) {
			case QUOTES:
				{
				setState(17); match(QUOTES);
				 byteregex.quotesafe = true;
				             
				}
				break;
			case CASE:
				{
				setState(19); match(CASE);
				 byteregex.casesensitive = true; 
				}
				break;
			case EOF:
			case LITERAL:
			case DOT:
			case END:
			case CHAR:
			case BRACKOPEN:
			case BLOCKOPEN:
			case START:
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(26);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LITERAL) | (1L << DOT) | (1L << END) | (1L << CHAR) | (1L << BRACKOPEN) | (1L << BLOCKOPEN) | (1L << START))) != 0)) {
				{
				setState(23); ((StartRuleContext)_localctx).frag = frag();
				 byteregex.root = ((StartRuleContext)_localctx).frag.fragout.start;
				               //log.info("startRule %s %s %d", ((StartRuleContext)_localctx).frag.fragout, ((StartRuleContext)_localctx).frag.fragout.start.type, ((StartRuleContext)_localctx).frag.fragout.end.size() ); 
				            
				}
			}

			setState(28); match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FragContext extends ParserRuleContext {
		public Fragment fragout;
		public StringContext string;
		public List<StringContext> string() {
			return getRuleContexts(StringContext.class);
		}
		public List<TerminalNode> OR() { return getTokens(ByteRegexParser.OR); }
		public StringContext string(int i) {
			return getRuleContext(StringContext.class,i);
		}
		public TerminalNode OR(int i) {
			return getToken(ByteRegexParser.OR, i);
		}
		public FragContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).enterFrag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).exitFrag(this);
		}
	}

	public final FragContext frag() throws RecognitionException {
		FragContext _localctx = new FragContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_frag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30); ((FragContext)_localctx).string = string();
			 ((FragContext)_localctx).fragout =  ((FragContext)_localctx).string.fragout; 
			setState(38);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(32); match(OR);
				setState(33); ((FragContext)_localctx).string = string();

				      if ( _localctx.fragout.start.type == TYPE.CHOICE ) {
				        Node next[] = new Node[ _localctx.fragout.start.next.length + 1 ];
				        System.arraycopy( _localctx.fragout.start.next, 0, next, 0, _localctx.fragout.start.next.length );
				        next[ _localctx.fragout.start.next.length ] = ((FragContext)_localctx).string.fragout.start;
				        _localctx.fragout.start.next = next;
				      } else {
				        Node node = new Node( TYPE.CHOICE, byteregex.casesensitive );
				        node.next = new Node[2];
				        node.next[0] = _localctx.fragout.start;
				        node.next[1] = ((FragContext)_localctx).string.fragout.start;
				        (_localctx.fragout).start = node;
				        _localctx.fragout.addEnd( ((FragContext)_localctx).string.fragout );
				      } 
				   
				}
				}
				setState(40);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
			 
			            //log.info("frag %s %s %d", _localctx.fragout, (_localctx.fragout).start.type, (_localctx.fragout).end.size() ); 
			          
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringContext extends ParserRuleContext {
		public Fragment fragout;
		public OperandContext operand;
		public OperandContext operand(int i) {
			return getRuleContext(OperandContext.class,i);
		}
		public List<OperandContext> operand() {
			return getRuleContexts(OperandContext.class);
		}
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).exitString(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_string);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(44); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(41); ((StringContext)_localctx).operand = operand();
				 
				             if (_localctx.fragout == null)
				                ((StringContext)_localctx).fragout =  ((StringContext)_localctx).operand.fragout;
				             else {
				                _localctx.fragout.setEnds( ((StringContext)_localctx).operand.fragout.start );
				                _localctx.fragout.addEnd( ((StringContext)_localctx).operand.fragout ); 
				             }
				           
				}
				}
				setState(46); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LITERAL) | (1L << DOT) | (1L << END) | (1L << CHAR) | (1L << BRACKOPEN) | (1L << BLOCKOPEN) | (1L << START))) != 0) );
			}
			 
			            //log.info("string %s %s %d", _localctx.fragout, (_localctx.fragout).start.type, (_localctx.fragout).end.size() ); 
			          
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OperandContext extends ParserRuleContext {
		public Fragment fragout;
		public SetContext set;
		public TerminalNode STAR() { return getToken(ByteRegexParser.STAR, 0); }
		public TerminalNode QUES() { return getToken(ByteRegexParser.QUES, 0); }
		public TerminalNode PLUS() { return getToken(ByteRegexParser.PLUS, 0); }
		public SetContext set() {
			return getRuleContext(SetContext.class,0);
		}
		public OperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).enterOperand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).exitOperand(this);
		}
	}

	public final OperandContext operand() throws RecognitionException {
		OperandContext _localctx = new OperandContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_operand);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48); ((OperandContext)_localctx).set = set();
			 ((OperandContext)_localctx).fragout =  ((OperandContext)_localctx).set.fragout; 
			setState(71);
			switch (_input.LA(1)) {
			case QUES:
				{
				{
				setState(50); match(QUES);
				 
				           Node node = new Node( TYPE.CHOICE, byteregex.casesensitive );
				           node.next = new Node[2];
				           node.next[0] = _localctx.fragout.start;
				             
				setState(54);
				_la = _input.LA(1);
				if (_la==QUES) {
					{
					setState(52); match(QUES);
					 node.next[1] = node.next[0]; node.next[0] = null; 
					}
				}

				 _localctx.fragout.addEnd( node );
				             (_localctx.fragout).start = node;
				        
				}
				}
				break;
			case STAR:
				{
				{
				setState(57); match(STAR);

				           Node node = new Node( TYPE.CHOICE, byteregex.casesensitive );
				           node.next = new Node[2];
				           node.next[0] = _localctx.fragout.start;
				             
				setState(61);
				_la = _input.LA(1);
				if (_la==QUES) {
					{
					setState(59); match(QUES);
					 node.next[1] = node.next[0]; node.next[0] = null; 
					}
				}

				           _localctx.fragout.setEnds( node );
				           (_localctx.fragout).start = node;
				           _localctx.fragout.addEnd(node);
				      
				}
				}
				break;
			case PLUS:
				{
				{
				setState(64); match(PLUS);

				           Node node = new Node( TYPE.CHOICE, byteregex.casesensitive );
				           node.next = new Node[2];
				           node.next[0] = _localctx.fragout.start;
				             
				setState(68);
				_la = _input.LA(1);
				if (_la==QUES) {
					{
					setState(66); match(QUES);
					 node.next[1] = node.next[0]; node.next[0] = null; 
					}
				}

				  _localctx.fragout.setEnds( node );
				              _localctx.fragout.addEnd( node );
				       
				}
				}
				break;
			case EOF:
			case OR:
			case LITERAL:
			case DOT:
			case END:
			case CHAR:
			case BRACKOPEN:
			case BRACKCLOSE:
			case BLOCKOPEN:
			case START:
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
			 
			            //log.info("operand %s %s %d", _localctx.fragout, (_localctx.fragout).start.type, (_localctx.fragout).end.size() ); 
			          
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SetContext extends ParserRuleContext {
		public Fragment fragout;
		public BracketContext bracket;
		public FragContext frag;
		public FragContext frag() {
			return getRuleContext(FragContext.class,0);
		}
		public TerminalNode BRACKOPEN() { return getToken(ByteRegexParser.BRACKOPEN, 0); }
		public TerminalNode BRACKCLOSE() { return getToken(ByteRegexParser.BRACKCLOSE, 0); }
		public TerminalNode LOOKAHEAD() { return getToken(ByteRegexParser.LOOKAHEAD, 0); }
		public BracketContext bracket() {
			return getRuleContext(BracketContext.class,0);
		}
		public TerminalNode LOOKBEHIND() { return getToken(ByteRegexParser.LOOKBEHIND, 0); }
		public SetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).enterSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).exitSet(this);
		}
	}

	public final SetContext set() throws RecognitionException {
		SetContext _localctx = new SetContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_set);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92);
			switch (_input.LA(1)) {
			case LITERAL:
			case DOT:
			case END:
			case CHAR:
			case BLOCKOPEN:
			case START:
				{
				setState(73); ((SetContext)_localctx).bracket = bracket();
				 ((SetContext)_localctx).fragout =  new Fragment();
				                 (_localctx.fragout).start = ((SetContext)_localctx).bracket.node;
				                 _localctx.fragout.addEnd( ((SetContext)_localctx).bracket.node );
				               
				}
				break;
			case BRACKOPEN:
				{
				setState(76); match(BRACKOPEN);
				setState(88);
				switch (_input.LA(1)) {
				case LITERAL:
				case DOT:
				case END:
				case CHAR:
				case BRACKOPEN:
				case BLOCKOPEN:
				case START:
					{
					setState(77); ((SetContext)_localctx).frag = frag();
					 ((SetContext)_localctx).fragout =  ((SetContext)_localctx).frag.fragout; 
					}
					break;
				case LOOKAHEAD:
					{
					{
					setState(80); match(LOOKAHEAD);
					setState(81); ((SetContext)_localctx).frag = frag();
					 
					            ((SetContext)_localctx).fragout =  ((SetContext)_localctx).frag.fragout;
					            Fragment f = new Fragment();
					            Node lookahead = new Node( TYPE.LOOKAHEAD, byteregex.casesensitive );
					            lookahead.next = new Node[2];
					            lookahead.next[0] = _localctx.fragout.start;
					            lookahead.next[1] = null;
					            _localctx.fragout.setEnds( null );
					            _localctx.fragout.addEnd( lookahead );
					            (_localctx.fragout).start = lookahead;
					           
					}
					}
					break;
				case LOOKBEHIND:
					{
					{
					setState(84); match(LOOKBEHIND);
					setState(85); ((SetContext)_localctx).frag = frag();
					 
					            ((SetContext)_localctx).fragout =  ((SetContext)_localctx).frag.fragout;
					            Fragment f = new Fragment();
					            Node lookahead = new Node( TYPE.LOOKBEHIND, byteregex.casesensitive );
					            lookahead.next = new Node[2];
					            lookahead.next[0] = _localctx.fragout.start;
					            lookahead.next[1] = null;
					            _localctx.fragout.setEnds( null );
					            _localctx.fragout.addEnd( lookahead );
					            (_localctx.fragout).start = lookahead;
					           
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(90); match(BRACKCLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
			 
			            //log.info("set %s %s %d", _localctx.fragout, (_localctx.fragout).start.type, (_localctx.fragout).end.size() ); 
			          
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BracketContext extends ParserRuleContext {
		public Node node;
		public CharacterContext character;
		public TerminalNode MINUS(int i) {
			return getToken(ByteRegexParser.MINUS, i);
		}
		public TerminalNode BLOCKCLOSE() { return getToken(ByteRegexParser.BLOCKCLOSE, 0); }
		public CharacterContext character(int i) {
			return getRuleContext(CharacterContext.class,i);
		}
		public List<TerminalNode> MINUS() { return getTokens(ByteRegexParser.MINUS); }
		public List<CharacterContext> character() {
			return getRuleContexts(CharacterContext.class);
		}
		public TerminalNode BLOCKOPEN() { return getToken(ByteRegexParser.BLOCKOPEN, 0); }
		public BracketContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bracket; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).enterBracket(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).exitBracket(this);
		}
	}

	public final BracketContext bracket() throws RecognitionException {
		BracketContext _localctx = new BracketContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_bracket);
		  boolean positive = true;
		            char lastcharacter = 0;
		   
		int _la;
		try {
			setState(114);
			switch (_input.LA(1)) {
			case LITERAL:
			case DOT:
			case END:
			case CHAR:
			case START:
				enterOuterAlt(_localctx, 1);
				{
				setState(94); ((BracketContext)_localctx).character = character();
				 ((BracketContext)_localctx).node =  ((BracketContext)_localctx).character.node; 
				}
				break;
			case BLOCKOPEN:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(97); match(BLOCKOPEN);
				setState(98); ((BracketContext)_localctx).character = character();
				 ((BracketContext)_localctx).node =  new Node( TYPE.CHAR, byteregex.casesensitive );
				                 (_localctx.node).next = new Node[1];
				                 if (((BracketContext)_localctx).character.node.type == TYPE.START) {
				                    positive=false;
				                 } else {
				                    lastcharacter = ((BracketContext)_localctx).character.node.allowedChar();
				                    _localctx.node.setAllowed( lastcharacter );
				                 }
				               
				setState(109);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LITERAL) | (1L << DOT) | (1L << END) | (1L << CHAR) | (1L << START) | (1L << MINUS))) != 0)) {
					{
					setState(107);
					switch (_input.LA(1)) {
					case MINUS:
						{
						{
						setState(100); match(MINUS);
						setState(101); ((BracketContext)_localctx).character = character();
						 
						          char nextcharacter = ((BracketContext)_localctx).character.node.allowedChar();
						          _localctx.node.setAllowedRange( (char)(lastcharacter + 1), nextcharacter);
						          lastcharacter = nextcharacter;
						       
						}
						}
						break;
					case LITERAL:
					case DOT:
					case END:
					case CHAR:
					case START:
						{
						setState(104); ((BracketContext)_localctx).character = character();
						 
						         //log.info("add bracket %s", ((BracketContext)_localctx).character.node.toString());
						         lastcharacter = ((BracketContext)_localctx).character.node.allowedChar();
						         _localctx.node.combineAllowed( ((BracketContext)_localctx).character.node.allowed );
						      
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(111);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(112); match(BLOCKCLOSE);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			 if (!positive)
			               _localctx.node.invertAllowed();
			             //log.info("bracket %s %s %d", _localctx.node, (_localctx.node).type, (_localctx.node).next.length ); 
			          
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CharacterContext extends ParserRuleContext {
		public Node node;
		public Token CHAR;
		public Token LITERAL;
		public TerminalNode DOT() { return getToken(ByteRegexParser.DOT, 0); }
		public TerminalNode START() { return getToken(ByteRegexParser.START, 0); }
		public TerminalNode LITERAL() { return getToken(ByteRegexParser.LITERAL, 0); }
		public TerminalNode CHAR() { return getToken(ByteRegexParser.CHAR, 0); }
		public TerminalNode END() { return getToken(ByteRegexParser.END, 0); }
		public CharacterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_character; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).enterCharacter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ByteRegexListener ) ((ByteRegexListener)listener).exitCharacter(this);
		}
	}

	public final CharacterContext character() throws RecognitionException {
		CharacterContext _localctx = new CharacterContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_character);
		 ((CharacterContext)_localctx).node =  new Node( TYPE.CHAR, byteregex.casesensitive );
		           (_localctx.node).next = new Node[1];
		         
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			switch (_input.LA(1)) {
			case CHAR:
				{
				setState(116); ((CharacterContext)_localctx).CHAR = match(CHAR);
				 _localctx.node.setAllowed( (((CharacterContext)_localctx).CHAR!=null?((CharacterContext)_localctx).CHAR.getText():null).charAt(0) ); 
				}
				break;
			case START:
				{
				setState(118); match(START);
				 (_localctx.node).type = TYPE.START; 
				}
				break;
			case END:
				{
				setState(120); match(END);
				 (_localctx.node).type = TYPE.END; 
				}
				break;
			case DOT:
				{
				setState(122); match(DOT);
				 _localctx.node.setAllowedRange( '\0', '~'); 
				}
				break;
			case LITERAL:
				{
				setState(124); ((CharacterContext)_localctx).LITERAL = match(LITERAL);
				 switch ((((CharacterContext)_localctx).LITERAL!=null?((CharacterContext)_localctx).LITERAL.getText():null).charAt(1)) {
				                   case 'g' :
				                      _localctx.node.setAllowed( ' ' );
				                      break;
				                   case 'n' :
				                      _localctx.node.setAllowedSet( '\r', '\n', (char)10 );
				                      break;
				                   case 't' :
				                      _localctx.node.setAllowedSet( '\t' );
				                      break;
				                   case 's' :
				                      _localctx.node.setAllowedSet( ' ', '\n', '\t', '\r' );
				                      break;
				                   case 'S' :
				                      _localctx.node.setAllowedSet( ' ', '\n', '\t', '\r' );
				                      _localctx.node.invertAllowed();
				                      break;
				                   case 'w' :
				                      _localctx.node.setAllowedRange( 'A', 'Z' );
				                      _localctx.node.setAllowedRange( 'a', 'z' );
				                      _localctx.node.setAllowedRange( '0', '9' );
				                      _localctx.node.setAllowed( '_' );
				                      break;
				                   case 'W' :
				                      _localctx.node.setAllowedRange( 'A', 'Z' );
				                      _localctx.node.setAllowedRange( 'a', 'z' );
				                      _localctx.node.setAllowedRange( '0', '9' );
				                      _localctx.node.setAllowed( '_' );
				                      _localctx.node.invertAllowed();
				                      break;
				                   case 'c' :
				                      _localctx.node.setAllowedRange( 'A', 'Z' );
				                      _localctx.node.setAllowedRange( 'a', 'z' );
				                      break;
				                   case 'C' :
				                      _localctx.node.setAllowedRange( 'A', 'Z' );
				                      _localctx.node.setAllowedRange( 'a', 'z' );
				                      _localctx.node.invertAllowed();
				                      break;
				                   case 'd' :
				                      _localctx.node.setAllowedRange( '0', '9' );
				                      break;
				                   case 'D' :
				                      _localctx.node.setAllowedRange( '0', '9' );
				                      _localctx.node.invertAllowed( );
				                      break;
				                   default:
				                      _localctx.node.setAllowed( (((CharacterContext)_localctx).LITERAL!=null?((CharacterContext)_localctx).LITERAL.getText():null).charAt(1) );
				               } 
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
			  //log.info("character %s %s %d", _localctx.node, (_localctx.node).type, (_localctx.node).next.length ); 
			         
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3\24\u0083\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\5\2\22\n\2\3\2\3"+
		"\2\3\2\3\2\5\2\30\n\2\3\2\3\2\3\2\5\2\35\n\2\3\2\3\2\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\7\3\'\n\3\f\3\16\3*\13\3\3\4\3\4\3\4\6\4/\n\4\r\4\16\4\60\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\5\59\n\5\3\5\3\5\3\5\3\5\3\5\5\5@\n\5\3\5\3\5\3\5\3"+
		"\5\3\5\5\5G\n\5\3\5\5\5J\n\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\5\6[\n\6\3\6\3\6\5\6_\n\6\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\7\7n\n\7\f\7\16\7q\13\7\3\7\3\7\5\7u\n\7\3"+
		"\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u0081\n\b\3\b\2\t\2\4\6\b\n"+
		"\f\16\2\2\u0091\2\21\3\2\2\2\4 \3\2\2\2\6.\3\2\2\2\b\62\3\2\2\2\n^\3\2"+
		"\2\2\ft\3\2\2\2\16\u0080\3\2\2\2\20\22\7\b\2\2\21\20\3\2\2\2\21\22\3\2"+
		"\2\2\22\27\3\2\2\2\23\24\7\5\2\2\24\30\b\2\1\2\25\26\7\6\2\2\26\30\b\2"+
		"\1\2\27\23\3\2\2\2\27\25\3\2\2\2\27\30\3\2\2\2\30\34\3\2\2\2\31\32\5\4"+
		"\3\2\32\33\b\2\1\2\33\35\3\2\2\2\34\31\3\2\2\2\34\35\3\2\2\2\35\36\3\2"+
		"\2\2\36\37\7\2\2\3\37\3\3\2\2\2 !\5\6\4\2!(\b\3\1\2\"#\7\3\2\2#$\5\6\4"+
		"\2$%\b\3\1\2%\'\3\2\2\2&\"\3\2\2\2\'*\3\2\2\2(&\3\2\2\2()\3\2\2\2)\5\3"+
		"\2\2\2*(\3\2\2\2+,\5\b\5\2,-\b\4\1\2-/\3\2\2\2.+\3\2\2\2/\60\3\2\2\2\60"+
		".\3\2\2\2\60\61\3\2\2\2\61\7\3\2\2\2\62\63\5\n\6\2\63I\b\5\1\2\64\65\7"+
		"\b\2\2\658\b\5\1\2\66\67\7\b\2\2\679\b\5\1\28\66\3\2\2\289\3\2\2\29:\3"+
		"\2\2\2:J\b\5\1\2;<\7\7\2\2<?\b\5\1\2=>\7\b\2\2>@\b\5\1\2?=\3\2\2\2?@\3"+
		"\2\2\2@A\3\2\2\2AJ\b\5\1\2BC\7\t\2\2CF\b\5\1\2DE\7\b\2\2EG\b\5\1\2FD\3"+
		"\2\2\2FG\3\2\2\2GH\3\2\2\2HJ\b\5\1\2I\64\3\2\2\2I;\3\2\2\2IB\3\2\2\2I"+
		"J\3\2\2\2J\t\3\2\2\2KL\5\f\7\2LM\b\6\1\2M_\3\2\2\2NZ\7\r\2\2OP\5\4\3\2"+
		"PQ\b\6\1\2Q[\3\2\2\2RS\7\23\2\2ST\5\4\3\2TU\b\6\1\2U[\3\2\2\2VW\7\24\2"+
		"\2WX\5\4\3\2XY\b\6\1\2Y[\3\2\2\2ZO\3\2\2\2ZR\3\2\2\2ZV\3\2\2\2[\\\3\2"+
		"\2\2\\]\7\16\2\2]_\3\2\2\2^K\3\2\2\2^N\3\2\2\2_\13\3\2\2\2`a\5\16\b\2"+
		"ab\b\7\1\2bu\3\2\2\2cd\7\17\2\2de\5\16\b\2eo\b\7\1\2fg\7\22\2\2gh\5\16"+
		"\b\2hi\b\7\1\2in\3\2\2\2jk\5\16\b\2kl\b\7\1\2ln\3\2\2\2mf\3\2\2\2mj\3"+
		"\2\2\2nq\3\2\2\2om\3\2\2\2op\3\2\2\2pr\3\2\2\2qo\3\2\2\2rs\7\20\2\2su"+
		"\3\2\2\2t`\3\2\2\2tc\3\2\2\2u\r\3\2\2\2vw\7\f\2\2w\u0081\b\b\1\2xy\7\21"+
		"\2\2y\u0081\b\b\1\2z{\7\13\2\2{\u0081\b\b\1\2|}\7\n\2\2}\u0081\b\b\1\2"+
		"~\177\7\4\2\2\177\u0081\b\b\1\2\u0080v\3\2\2\2\u0080x\3\2\2\2\u0080z\3"+
		"\2\2\2\u0080|\3\2\2\2\u0080~\3\2\2\2\u0081\17\3\2\2\2\21\21\27\34(\60"+
		"8?FIZ^mot\u0080";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}