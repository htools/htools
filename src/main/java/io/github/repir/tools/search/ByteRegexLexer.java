// Generated from ByteRegex.g4 by ANTLR 4.1

    package io.github.repir.tools.search;
import io.github.repir.tools.search.Node;
    import io.github.repir.tools.search.Node.*; 
    import java.util.ArrayList;
    import io.github.repir.tools.lib.Log;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ByteRegexLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		OR=1, LITERAL=2, CASE=3, STAR=4, QUES=5, PLUS=6, DOT=7, END=8, CHAR=9, 
		BRACKOPEN=10, BRACKCLOSE=11, BLOCKOPEN=12, BLOCKCLOSE=13, START=14, MINUS=15, 
		LOOKAHEAD=16, LOOKBEHIND=17;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"'|'", "LITERAL", "CASE", "'*'", "'?'", "'+'", "'.'", "'$'", "CHAR", "'('", 
		"')'", "'['", "']'", "'^'", "'-'", "LOOKAHEAD", "LOOKBEHIND"
	};
	public static final String[] ruleNames = {
		"OR", "LITERAL", "CASE", "STAR", "QUES", "PLUS", "DOT", "END", "CHAR", 
		"BRACKOPEN", "BRACKCLOSE", "BLOCKOPEN", "BLOCKCLOSE", "START", "MINUS", 
		"LOOKAHEAD", "LOOKBEHIND"
	};


	   public static Log log = new Log( ByteRegexParser.class );
	   private boolean casesensitive = false;
	   public Node root;


	public ByteRegexLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "ByteRegex.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\23L\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3"+
		"\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21"+
		"\3\21\3\21\3\22\3\22\3\22\3\22\2\23\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b"+
		"\1\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\16\1\33\17\1\35\20\1\37\21\1"+
		"!\22\1#\23\1\3\2\4\3\2EE\b\2&&*-/\60AA]]_`K\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2"+
		"\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\3%\3\2\2\2\5\'\3\2\2"+
		"\2\7*\3\2\2\2\t-\3\2\2\2\13/\3\2\2\2\r\61\3\2\2\2\17\63\3\2\2\2\21\65"+
		"\3\2\2\2\23\67\3\2\2\2\259\3\2\2\2\27;\3\2\2\2\31=\3\2\2\2\33?\3\2\2\2"+
		"\35A\3\2\2\2\37C\3\2\2\2!E\3\2\2\2#H\3\2\2\2%&\7~\2\2&\4\3\2\2\2\'(\7"+
		"^\2\2()\n\2\2\2)\6\3\2\2\2*+\7^\2\2+,\7E\2\2,\b\3\2\2\2-.\7,\2\2.\n\3"+
		"\2\2\2/\60\7A\2\2\60\f\3\2\2\2\61\62\7-\2\2\62\16\3\2\2\2\63\64\7\60\2"+
		"\2\64\20\3\2\2\2\65\66\7&\2\2\66\22\3\2\2\2\678\n\3\2\28\24\3\2\2\29:"+
		"\7*\2\2:\26\3\2\2\2;<\7+\2\2<\30\3\2\2\2=>\7]\2\2>\32\3\2\2\2?@\7_\2\2"+
		"@\34\3\2\2\2AB\7`\2\2B\36\3\2\2\2CD\7/\2\2D \3\2\2\2EF\7A\2\2FG\7?\2\2"+
		"G\"\3\2\2\2HI\7A\2\2IJ\7>\2\2JK\7?\2\2K$\3\2\2\2\3\2";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}