// Generated from ByteRegex.g4 by ANTLR 4.2.2

    package io.github.repir.tools.search;
    import io.github.repir.tools.search.Node.*; 
    import java.util.ArrayList;
    import io.github.repir.tools.lib.Log;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ByteRegexParser}.
 */
public interface ByteRegexListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ByteRegexParser#frag}.
	 * @param ctx the parse tree
	 */
	void enterFrag(@NotNull ByteRegexParser.FragContext ctx);
	/**
	 * Exit a parse tree produced by {@link ByteRegexParser#frag}.
	 * @param ctx the parse tree
	 */
	void exitFrag(@NotNull ByteRegexParser.FragContext ctx);

	/**
	 * Enter a parse tree produced by {@link ByteRegexParser#set}.
	 * @param ctx the parse tree
	 */
	void enterSet(@NotNull ByteRegexParser.SetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ByteRegexParser#set}.
	 * @param ctx the parse tree
	 */
	void exitSet(@NotNull ByteRegexParser.SetContext ctx);

	/**
	 * Enter a parse tree produced by {@link ByteRegexParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(@NotNull ByteRegexParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link ByteRegexParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(@NotNull ByteRegexParser.StringContext ctx);

	/**
	 * Enter a parse tree produced by {@link ByteRegexParser#operand}.
	 * @param ctx the parse tree
	 */
	void enterOperand(@NotNull ByteRegexParser.OperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link ByteRegexParser#operand}.
	 * @param ctx the parse tree
	 */
	void exitOperand(@NotNull ByteRegexParser.OperandContext ctx);

	/**
	 * Enter a parse tree produced by {@link ByteRegexParser#bracket}.
	 * @param ctx the parse tree
	 */
	void enterBracket(@NotNull ByteRegexParser.BracketContext ctx);
	/**
	 * Exit a parse tree produced by {@link ByteRegexParser#bracket}.
	 * @param ctx the parse tree
	 */
	void exitBracket(@NotNull ByteRegexParser.BracketContext ctx);

	/**
	 * Enter a parse tree produced by {@link ByteRegexParser#character}.
	 * @param ctx the parse tree
	 */
	void enterCharacter(@NotNull ByteRegexParser.CharacterContext ctx);
	/**
	 * Exit a parse tree produced by {@link ByteRegexParser#character}.
	 * @param ctx the parse tree
	 */
	void exitCharacter(@NotNull ByteRegexParser.CharacterContext ctx);

	/**
	 * Enter a parse tree produced by {@link ByteRegexParser#startRule}.
	 * @param ctx the parse tree
	 */
	void enterStartRule(@NotNull ByteRegexParser.StartRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link ByteRegexParser#startRule}.
	 * @param ctx the parse tree
	 */
	void exitStartRule(@NotNull ByteRegexParser.StartRuleContext ctx);
}