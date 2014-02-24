// Generated from ByteRegex.g4 by ANTLR 4.1

    package io.github.repir.tools.ByteRegex;
    import io.github.repir.tools.ByteRegex.Node.*; 
    import java.util.ArrayList;
    import io.github.repir.tools.Lib.Log;

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
		OR=1, LITERAL=2, QUOTES=3, CASE=4, STAR=5, QUES=6, PLUS=7, DOT=8, END=9, 
		CHAR=10, BRACKOPEN=11, BRACKCLOSE=12, BLOCKOPEN=13, BLOCKCLOSE=14, START=15, 
		MINUS=16, LOOKAHEAD=17, LOOKBEHIND=18;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"'|'", "LITERAL", "QUOTES", "CASE", "'*'", "'?'", "'+'", "'.'", "'$'", 
		"CHAR", "'('", "')'", "'['", "']'", "'^'", "'-'", "LOOKAHEAD", "LOOKBEHIND"
	};
	public static final String[] ruleNames = {
		"OR", "LITERAL", "QUOTES", "CASE", "STAR", "QUES", "PLUS", "DOT", "END", 
		"CHAR", "BRACKOPEN", "BRACKCLOSE", "BLOCKOPEN", "BLOCKCLOSE", "START", 
		"MINUS", "LOOKAHEAD", "LOOKBEHIND"
	};


	   public static Log log = new Log( ByteRegexParser.class );
	   public ByteRegex byteregex;


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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\24Q\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\7\3\7"+
		"\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17"+
		"\3\20\3\20\3\21\3\21\3\22\3\22\3\22\3\23\3\23\3\23\3\23\2\24\3\3\1\5\4"+
		"\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\16"+
		"\1\33\17\1\35\20\1\37\21\1!\22\1#\23\1%\24\1\3\2\4\4\2EESS\b\2&&*-/\60"+
		"AA]]_`P\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2"+
		"\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3"+
		"\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2"+
		"\2#\3\2\2\2\2%\3\2\2\2\3\'\3\2\2\2\5)\3\2\2\2\7,\3\2\2\2\t/\3\2\2\2\13"+
		"\62\3\2\2\2\r\64\3\2\2\2\17\66\3\2\2\2\218\3\2\2\2\23:\3\2\2\2\25<\3\2"+
		"\2\2\27>\3\2\2\2\31@\3\2\2\2\33B\3\2\2\2\35D\3\2\2\2\37F\3\2\2\2!H\3\2"+
		"\2\2#J\3\2\2\2%M\3\2\2\2\'(\7~\2\2(\4\3\2\2\2)*\7^\2\2*+\n\2\2\2+\6\3"+
		"\2\2\2,-\7^\2\2-.\7S\2\2.\b\3\2\2\2/\60\7^\2\2\60\61\7E\2\2\61\n\3\2\2"+
		"\2\62\63\7,\2\2\63\f\3\2\2\2\64\65\7A\2\2\65\16\3\2\2\2\66\67\7-\2\2\67"+
		"\20\3\2\2\289\7\60\2\29\22\3\2\2\2:;\7&\2\2;\24\3\2\2\2<=\n\3\2\2=\26"+
		"\3\2\2\2>?\7*\2\2?\30\3\2\2\2@A\7+\2\2A\32\3\2\2\2BC\7]\2\2C\34\3\2\2"+
		"\2DE\7_\2\2E\36\3\2\2\2FG\7`\2\2G \3\2\2\2HI\7/\2\2I\"\3\2\2\2JK\7A\2"+
		"\2KL\7?\2\2L$\3\2\2\2MN\7A\2\2NO\7>\2\2OP\7?\2\2P&\3\2\2\2\3\2";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}