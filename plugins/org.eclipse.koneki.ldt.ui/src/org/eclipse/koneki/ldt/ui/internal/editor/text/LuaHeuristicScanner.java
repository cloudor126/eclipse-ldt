/*******************************************************************************
 * Copyright (c) 2013 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.ui.internal.editor.text;

import java.util.Arrays;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.Item;
import org.eclipse.koneki.ldt.core.internal.ast.models.api.UnknownItem;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Call;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Identifier;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Index;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.Invoke;
import org.eclipse.koneki.ldt.core.internal.ast.models.file.LuaExpression;

/**
 * Utility methods for heuristic based Lua manipulations in an incomplete Lua source file.
 * 
 * <p>
 * An instance holds some internal position in the document and is therefore not threadsafe.
 * </p>
 * 
 * inspired by org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner
 */
public class LuaHeuristicScanner {

	/**
	 * Returned by all methods when the requested position could not be found, or if a {@link BadLocationException} was thrown while scanning.
	 */
	public static final int NOT_FOUND = -1;

	/**
	 * Special bound parameter that means either -1 (backward scanning) or <code>fDocument.getLength()</code> (forward scanning).
	 */
	public static final int UNBOUND = -2;

	/* character constants */
	private static final char LBRACE = '{';
	private static final char RBRACE = '}';
	private static final char LBRACKET = '[';
	private static final char RBRACKET = ']';
	private static final char LPAREN = '(';
	private static final char RPAREN = ')';

	private static final char SEMICOLON = ';';
	private static final char COMMA = ',';

	private static final char COLON = ':';
	private static final char DOT = '.';

	private static final char EQUAL = '=';
	private static final char LANGLE = '<';
	private static final char RANGLE = '>';

	private static final char PLUS = '+';
	private static final char MINUS = '-';
	private static final char ASTERISK = '*';
	private static final char SLASH = '/';
	private static final char PERSENT = '%';
	private static final char CARET = '^';

	private static final char SHARP = '#';

	/* preset stop conditions */
	private static final StopCondition FNONWS = new NonWhitespace();
	private final StopCondition fNonWSDefaultPart = new NonWhitespaceDefaultPartition();
	private final StopCondition fNonIdent = new NonJavaIdentifierPartDefaultPartition();

	/**
	 * Specifies the stop condition, upon which the <code>scanXXX</code> methods will decide whether to keep scanning or not. This interface may
	 * implemented by clients.
	 */
	private abstract static class StopCondition {
		/**
		 * Instructs the scanner to return the current position.
		 * 
		 * @param ch
		 *            the char at the current position
		 * @param position
		 *            the current position
		 * @param forward
		 *            the iteration direction
		 * @return <code>true</code> if the stop condition is met.
		 */
		public abstract boolean stop(char ch, int position, boolean forward);

		/**
		 * Asks the condition to return the next position to query. The default is to return the next/previous position.
		 * 
		 * @param position
		 *            the position
		 * @param forward
		 *            <code>true</code> if next position should be returned
		 * @return the next position to scan
		 */
		public int nextPosition(int position, boolean forward) {
			return forward ? position + 1 : position - 1;
		}
	}

	/**
	 * Stops upon a non-whitespace (as defined by {@link Character#isWhitespace(char)}) character.
	 */
	private static class NonWhitespace extends StopCondition {
		@Override
		public boolean stop(char ch, int position, boolean forward) {
			return !Character.isWhitespace(ch);
		}
	}

	/**
	 * Stops upon a non-whitespace character in the default partition.
	 */
	private final class NonWhitespaceDefaultPartition extends NonWhitespace {
		@Override
		public boolean stop(char ch, int position, boolean forward) {
			return super.stop(ch, position, true) && isAcceptedPartition(position);
		}

		@Override
		public int nextPosition(int position, boolean forward) {
			ITypedRegion partition = getPartition(position);
			if (isAcceptedPartition(partition))
				return super.nextPosition(position, forward);

			if (forward) {
				int end = partition.getOffset() + partition.getLength();
				if (position < end)
					return end;
			} else {
				int offset = partition.getOffset();
				if (position > offset)
					return offset - 1;
			}
			return super.nextPosition(position, forward);
		}
	}

	/**
	 * Stops upon a non-java identifier (as defined by {@link Character#isJavaIdentifierPart(char)}) character.
	 */
	private static class NonJavaIdentifierPart extends StopCondition {
		@Override
		public boolean stop(char ch, int position, boolean forward) {
			return !Character.isJavaIdentifierPart(ch);
		}
	}

	/**
	 * Stops upon a non-java identifier character in the default partition.
	 * 
	 * @see JavaHeuristicScanner.NonJavaIdentifierPart
	 */
	private final class NonJavaIdentifierPartDefaultPartition extends NonJavaIdentifierPart {
		@Override
		public boolean stop(char ch, int position, boolean forward) {
			return super.stop(ch, position, true) || !isAcceptedPartition(position);
		}

		@Override
		public int nextPosition(int position, boolean forward) {
			ITypedRegion partition = getPartition(position);
			if (isAcceptedPartition(partition))
				return super.nextPosition(position, forward);

			if (forward) {
				int end = partition.getOffset() + partition.getLength();
				if (position < end)
					return end;
			} else {
				int offset = partition.getOffset();
				if (position > offset)
					return offset - 1;
			}
			return super.nextPosition(position, forward);
		}
	}

	/**
	 * Stops upon a character in the default partition that matches the given character list.
	 */
	private final class CharacterMatch extends StopCondition {
		private final char[] fChars;

		/**
		 * Creates a new instance.
		 * 
		 * @param ch
		 *            the single character to match
		 */
		public CharacterMatch(char ch) {
			this(new char[] { ch });
		}

		/**
		 * Creates a new instance.
		 * 
		 * @param chars
		 *            the chars to match.
		 */
		public CharacterMatch(char[] chars) {
			Assert.isNotNull(chars);
			Assert.isTrue(chars.length > 0);
			fChars = chars;
			Arrays.sort(chars);
		}

		@Override
		public boolean stop(char ch, int position, boolean forward) {
			return Arrays.binarySearch(fChars, ch) >= 0 && isAcceptedPartition(position);
		}

		@Override
		public int nextPosition(int position, boolean forward) {
			ITypedRegion partition = getPartition(position);
			if (isAcceptedPartition(partition))
				return super.nextPosition(position, forward);

			if (forward) {
				int end = partition.getOffset() + partition.getLength();
				if (position < end)
					return end;
			} else {
				int offset = partition.getOffset();
				if (position > offset)
					return offset - 1;
			}
			return super.nextPosition(position, forward);
		}
	}

	/** The document being scanned. */
	private final IDocument fDocument;

	/* internal scan state */

	/** the most recently read character. */
	private char fChar;
	/** the most recently read position. */
	private int fPos;

	/**
	 * The most recently used partition.
	 */
	private ITypedRegion fCachedPartition = new TypedRegion(-1, 0, "__no_partition_at_all"); //$NON-NLS-1$

	/**
	 * Calls <code>this(document, ILuaPartitions.LUA_PARTITIONING, IDocument.DEFAULT_CONTENT_TYPE)</code>.
	 * 
	 * @param document
	 *            the document to scan.
	 */
	public LuaHeuristicScanner(IDocument document) {
		Assert.isLegal(document != null);
		fDocument = document;
	}

	/**
	 * Returns the most recent internal scan position.
	 * 
	 * @return the most recent internal scan position.
	 */
	public int getPosition() {
		return fPos;
	}

	/**
	 * Returns the next token in forward direction, starting at <code>start</code>, and not extending further than <code>bound</code>. The return
	 * value is one of the constants defined in {@link Symbols}. After a call, {@link #getPosition()} will return the position just after the scanned
	 * token (i.e. the next position that will be scanned).
	 * 
	 * @param start
	 *            the first character position in the document to consider
	 * @param bound
	 *            the first position not to consider any more
	 * @return a constant from {@link Symbols} describing the next token
	 */
	public int nextToken(int start, int bound) {
		int pos = scanForward(start, bound, fNonWSDefaultPart);
		if (pos == NOT_FOUND)
			return LuaSymbols.TOKEN_EOF;

		// first, detect string
		ITypedRegion partition = getPartition(pos);
		if (partition.getType().equals(ILuaPartitions.LUA_STRING) || partition.getType().equals(ILuaPartitions.LUA_SINGLE_QUOTE_STRING)
				|| partition.getType().equals(ILuaPartitions.LUA_MULTI_LINE_STRING)) {
			fPos += partition.getLength();
			return LuaSymbols.TOKEN_STRING;

		}

		fPos++;

		// detect key char
		switch (fChar) {
		case LBRACE:
			return LuaSymbols.TOKEN_LBRACE;
		case RBRACE:
			return LuaSymbols.TOKEN_RBRACE;
		case LBRACKET:
			return LuaSymbols.TOKEN_LBRACKET;
		case RBRACKET:
			return LuaSymbols.TOKEN_RBRACKET;
		case LPAREN:
			return LuaSymbols.TOKEN_LPAREN;
		case RPAREN:
			return LuaSymbols.TOKEN_RPAREN;
		case SEMICOLON:
			return LuaSymbols.TOKEN_SEMICOLON;
		case COMMA:
			return LuaSymbols.TOKEN_COMMA;
		case EQUAL:
			return LuaSymbols.TOKEN_EQUAL;
		case LANGLE:
			return LuaSymbols.TOKEN_LESSTHAN;
		case RANGLE:
			return LuaSymbols.TOKEN_GREATERTHAN;
		case PLUS:
			return LuaSymbols.TOKEN_PLUS;
		case MINUS:
			return LuaSymbols.TOKEN_MINUS;
		case SHARP:
			return LuaSymbols.TOKEN_SHARP;
		case DOT:
			return LuaSymbols.TOKEN_DOT;
		case COLON:
			return LuaSymbols.TOKEN_COLON;
		case ASTERISK:
			return LuaSymbols.TOKEN_ASTERISK;
		case SLASH:
			return LuaSymbols.TOKEN_SLASH;
		case PERSENT:
			return LuaSymbols.TOKEN_PERSENT;
		case CARET:
			return LuaSymbols.TOKEN_CARET;
		default:
			break;
		}

		// else
		if (Character.isJavaIdentifierPart(fChar)) {
			// assume an identifier or keyword
			int from = pos;
			int to;
			pos = scanForward(pos + 1, bound, fNonIdent);
			if (pos == NOT_FOUND)
				to = bound == UNBOUND ? fDocument.getLength() : bound;
			else
				to = pos;

			String identOrKeyword;
			try {
				identOrKeyword = fDocument.get(from, to - from);
			} catch (BadLocationException e) {
				return LuaSymbols.TOKEN_EOF;
			}

			return getToken(identOrKeyword);

		} else {
			// operators, number literals etc
			return LuaSymbols.TOKEN_OTHER;
		}
	}

	/**
	 * Returns the next token in backward direction, starting at <code>start</code>, and not extending further than <code>bound</code>. The return
	 * value is one of the constants defined in {@link Symbols}. After a call, {@link #getPosition()} will return the position just before the scanned
	 * token starts (i.e. the next position that will be scanned).
	 * 
	 * @param start
	 *            the first character position in the document to consider
	 * @param bound
	 *            the first position not to consider any more
	 * @return a constant from {@link Symbols} describing the previous token
	 */
	public int previousToken(int start, int bound) {
		int pos = scanBackward(start, bound, fNonWSDefaultPart);
		if (pos == NOT_FOUND)
			return LuaSymbols.TOKEN_EOF;

		// first, detect string
		ITypedRegion partition = getPartition(pos);
		if (partition.getType().equals(ILuaPartitions.LUA_STRING) || partition.getType().equals(ILuaPartitions.LUA_SINGLE_QUOTE_STRING)
				|| partition.getType().equals(ILuaPartitions.LUA_MULTI_LINE_STRING)) {
			fPos -= partition.getLength();
			return LuaSymbols.TOKEN_STRING;

		}

		fPos--;

		// detect key char
		switch (fChar) {
		case LBRACE:
			return LuaSymbols.TOKEN_LBRACE;
		case RBRACE:
			return LuaSymbols.TOKEN_RBRACE;
		case LBRACKET:
			return LuaSymbols.TOKEN_LBRACKET;
		case RBRACKET:
			return LuaSymbols.TOKEN_RBRACKET;
		case LPAREN:
			return LuaSymbols.TOKEN_LPAREN;
		case RPAREN:
			return LuaSymbols.TOKEN_RPAREN;
		case SEMICOLON:
			return LuaSymbols.TOKEN_SEMICOLON;
		case COLON:
			return LuaSymbols.TOKEN_COLON;
		case COMMA:
			return LuaSymbols.TOKEN_COMMA;
		case EQUAL:
			return LuaSymbols.TOKEN_EQUAL;
		case LANGLE:
			return LuaSymbols.TOKEN_LESSTHAN;
		case RANGLE:
			return LuaSymbols.TOKEN_GREATERTHAN;
		case PLUS:
			return LuaSymbols.TOKEN_PLUS;
		case MINUS:
			return LuaSymbols.TOKEN_MINUS;
		case SHARP:
			return LuaSymbols.TOKEN_SHARP;
		case DOT:
			return LuaSymbols.TOKEN_DOT;
		case ASTERISK:
			return LuaSymbols.TOKEN_ASTERISK;
		case SLASH:
			return LuaSymbols.TOKEN_SLASH;
		case PERSENT:
			return LuaSymbols.TOKEN_PERSENT;
		case CARET:
			return LuaSymbols.TOKEN_CARET;
		default:
			break;
		}

		// else
		if (Character.isJavaIdentifierPart(fChar)) {
			// assume an ident or keyword
			int from;
			int to = pos + 1;

			pos = scanBackward(pos - 1, bound, fNonIdent);
			if (pos == NOT_FOUND)
				from = bound == UNBOUND ? 0 : bound + 1;
			else
				from = pos + 1;

			String identOrKeyword;
			try {
				identOrKeyword = fDocument.get(from, to - from);
			} catch (BadLocationException e) {
				return LuaSymbols.TOKEN_EOF;
			}

			return getToken(identOrKeyword);

		} else {
			// operators, number literals etc
			return LuaSymbols.TOKEN_OTHER;
		}

	}

	/**
	 * Returns one of the keyword constants or <code>TOKEN_IDENT</code> for a scanned identifier.
	 * 
	 * @param s
	 *            a scanned identifier
	 * @return one of the constants defined in {@link Symbols}
	 */
	private int getToken(String s) {
		Assert.isNotNull(s);

		switch (s.length()) {
		case 2:
			if ("if".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_IF;
			if ("do".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_DO;
			if ("or".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_OR;
			break;
		case 3:
			if ("for".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_FOR;
			if ("end".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_END;
			if ("nil".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_NIL;
			if ("and".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_AND;
			if ("not".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_NOT;
			break;
		case 4:
			if ("else".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_ELSE;
			if ("then".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_THEN;
			if ("true".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_TRUE;
			break;
		case 5:
			if ("break".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_BREAK;
			if ("while".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_WHILE;
			if ("local".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_LOCAL;
			if ("false".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_FALSE;
			break;
		case 6:
			if ("return".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_RETURN;
			if ("repeat".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_REPEAT;
			if ("elseif".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_ELSEIF;
			break;
		case 8:
			if ("function".equals(s)) //$NON-NLS-1$
				return LuaSymbols.TOKEN_FUNCTION;
			break;
		default:
			break;
		}
		return LuaSymbols.TOKEN_IDENT;
	}

	/**
	 * Finds the smallest position in <code>fDocument</code> such that the position is &gt;= <code>position</code> and &lt; <code>bound</code> and
	 * <code>Character.isWhitespace(fDocument.getChar(pos))</code> evaluates to <code>false</code> and the position is in the default partition.
	 * 
	 * @param position
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &gt; <code>position</code>, or
	 *            <code>UNBOUND</code>
	 * @return the smallest position of a non-whitespace character in [<code>position</code>, <code>bound</code>) that resides in a Lua partition, or
	 *         <code>NOT_FOUND</code> if none can be found
	 */
	public int findNonWhitespaceForward(int position, int bound) {
		return scanForward(position, bound, fNonWSDefaultPart);
	}

	/**
	 * Finds the smallest position in <code>fDocument</code> such that the position is &gt;= <code>position</code> and &lt; <code>bound</code> and
	 * <code>Character.isWhitespace(fDocument.getChar(pos))</code> evaluates to <code>false</code>.
	 * 
	 * @param position
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &gt; <code>position</code>, or
	 *            <code>UNBOUND</code>
	 * @return the smallest position of a non-whitespace character in [<code>position</code>, <code>bound</code>), or <code>NOT_FOUND</code> if none
	 *         can be found
	 */
	public int findNonWhitespaceForwardInAnyPartition(int position, int bound) {
		return scanForward(position, bound, FNONWS);
	}

	/**
	 * Finds the highest position in <code>fDocument</code> such that the position is &lt;= <code>position</code> and &gt; <code>bound</code> and
	 * <code>Character.isWhitespace(fDocument.getChar(pos))</code> evaluates to <code>false</code> and the position is in the default partition.
	 * 
	 * @param position
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &lt; <code>position</code>, or
	 *            <code>UNBOUND</code>
	 * @return the highest position of a non-whitespace character in (<code>bound</code>, <code>position</code>] that resides in a Lua partition, or
	 *         <code>NOT_FOUND</code> if none can be found
	 */
	public int findNonWhitespaceBackward(int position, int bound) {
		return scanBackward(position, bound, fNonWSDefaultPart);
	}

	/**
	 * Finds the highest position in <code>fDocument</code> such that the position is &lt;= <code>position</code> and &gt; <code>bound</code> and
	 * <code>Character.isWhitespace(fDocument.getChar(pos))</code> evaluates to <code>false</code> and the position can be in any partition.
	 * 
	 * @param position
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &lt; <code>position</code>, or
	 *            <code>UNBOUND</code>
	 * @return the highest position of a non-whitespace character in (<code>bound</code>, <code>position</code>] that resides in a Lua partition, or
	 *         <code>NOT_FOUND</code> if none can be found
	 */
	public int findNonWhitespaceBackwardInAnyPartition(int position, int bound) {
		return scanBackward(position, bound, FNONWS);
	}

	/**
	 * Finds the lowest position <code>p</code> in <code>fDocument</code> such that <code>start</code> &lt;= p &lt; <code>bound</code> and
	 * <code>condition.stop(fDocument.getChar(p), p)</code> evaluates to <code>true</code>.
	 * 
	 * @param start
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &gt; <code>start</code>, or
	 *            <code>UNBOUND</code>
	 * @param condition
	 *            the <code>StopCondition</code> to check
	 * @return the lowest position in [<code>start</code>, <code>bound</code>) for which <code>condition</code> holds, or <code>NOT_FOUND</code> if
	 *         none can be found
	 */
	public int scanForward(int start, int bound, StopCondition condition) {
		Assert.isLegal(start >= 0);

		if (bound == UNBOUND)
			bound = fDocument.getLength();

		Assert.isLegal(bound <= fDocument.getLength());

		try {
			fPos = start;
			while (fPos < bound) {

				fChar = fDocument.getChar(fPos);
				if (condition.stop(fChar, fPos, true))
					return fPos;

				fPos = condition.nextPosition(fPos, true);
			}
		} catch (BadLocationException e) {
		}
		return NOT_FOUND;
	}

	/**
	 * Finds the lowest position in <code>fDocument</code> such that the position is &gt;= <code>position</code> and &lt; <code>bound</code> and
	 * <code>fDocument.getChar(position) == ch</code> evaluates to <code>true</code> and the position is in the default partition.
	 * 
	 * @param position
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &gt; <code>position</code>, or
	 *            <code>UNBOUND</code>
	 * @param ch
	 *            the <code>char</code> to search for
	 * @return the lowest position of <code>ch</code> in (<code>bound</code>, <code>position</code>] that resides in a Lua partition, or
	 *         <code>NOT_FOUND</code> if none can be found
	 */
	public int scanForward(int position, int bound, char ch) {
		return scanForward(position, bound, new CharacterMatch(ch));
	}

	/**
	 * Finds the lowest position in <code>fDocument</code> such that the position is &gt;= <code>position</code> and &lt; <code>bound</code> and
	 * <code>fDocument.getChar(position) == ch</code> evaluates to <code>true</code> for at least one ch in <code>chars</code> and the position is in
	 * the default partition.
	 * 
	 * @param position
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &gt; <code>position</code>, or
	 *            <code>UNBOUND</code>
	 * @param chars
	 *            an array of <code>char</code> to search for
	 * @return the lowest position of a non-whitespace character in [<code>position</code>, <code>bound</code>) that resides in a Lua partition, or
	 *         <code>NOT_FOUND</code> if none can be found
	 */
	public int scanForward(int position, int bound, char[] chars) {
		return scanForward(position, bound, new CharacterMatch(chars));
	}

	/**
	 * Finds the highest position <code>p</code> in <code>fDocument</code> such that <code>bound</code> &lt; <code>p</code> &lt;= <code>start</code>
	 * and <code>condition.stop(fDocument.getChar(p), p)</code> evaluates to <code>true</code>.
	 * 
	 * @param start
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &lt; <code>start</code>, or
	 *            <code>UNBOUND</code>
	 * @param condition
	 *            the <code>StopCondition</code> to check
	 * @return the highest position in (<code>bound</code>, <code>start</code> for which <code>condition</code> holds, or <code>NOT_FOUND</code> if
	 *         none can be found
	 */
	public int scanBackward(int start, int bound, StopCondition condition) {
		if (bound == UNBOUND)
			bound = -1;

		Assert.isLegal(bound >= -1);
		Assert.isLegal(start < fDocument.getLength());

		try {
			fPos = start;
			while (fPos > bound) {

				fChar = fDocument.getChar(fPos);
				if (condition.stop(fChar, fPos, false))
					return fPos;

				fPos = condition.nextPosition(fPos, false);
			}
			// CHECKSTYLE:OFF
		} catch (BadLocationException e) {
			// CHECKSTYLE:ON
		}
		return NOT_FOUND;
	}

	/**
	 * Finds the highest position in <code>fDocument</code> such that the position is &lt;= <code>position</code> and &gt; <code>bound</code> and
	 * <code>fDocument.getChar(position) == ch</code> evaluates to <code>true</code> for at least one ch in <code>chars</code> and the position is in
	 * the default partition.
	 * 
	 * @param position
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &lt; <code>position</code>, or
	 *            <code>UNBOUND</code>
	 * @param ch
	 *            the <code>char</code> to search for
	 * @return the highest position of one element in <code>chars</code> in (<code>bound</code>, <code>position</code>] that resides in a Lua
	 *         partition, or <code>NOT_FOUND</code> if none can be found
	 */
	public int scanBackward(int position, int bound, char ch) {
		return scanBackward(position, bound, new CharacterMatch(ch));
	}

	/**
	 * Finds the highest position in <code>fDocument</code> such that the position is &lt;= <code>position</code> and &gt; <code>bound</code> and
	 * <code>fDocument.getChar(position) == ch</code> evaluates to <code>true</code> for at least one ch in <code>chars</code> and the position is in
	 * the default partition.
	 * 
	 * @param position
	 *            the first character position in <code>fDocument</code> to be considered
	 * @param bound
	 *            the first position in <code>fDocument</code> to not consider any more, with <code>bound</code> &lt; <code>position</code>, or
	 *            <code>UNBOUND</code>
	 * @param chars
	 *            an array of <code>char</code> to search for
	 * @return the highest position of one element in <code>chars</code> in (<code>bound</code>, <code>position</code>] that resides in a Lua
	 *         partition, or <code>NOT_FOUND</code> if none can be found
	 */
	public int scanBackward(int position, int bound, char[] chars) {
		return scanBackward(position, bound, new CharacterMatch(chars));
	}

	/**
	 * Checks whether <code>position</code> resides in a default (Lua) partition of <code>fDocument</code> or in the LUA string partition.
	 * 
	 * @param position
	 *            the position to be checked
	 * @return <code>true</code> if <code>position</code> is in the default partition of <code>fDocument</code> or in the Lua string partition,
	 *         <code>false</code> otherwise
	 */
	public boolean isAcceptedPartition(int position) {
		return isAcceptedPartition(getPartition(position));
	}

	/**
	 * Checks whether <code>partition</code> is a default (Lua) partition of <code>fDocument</code> or the LUA string partition.
	 * 
	 * @param partition
	 *            the partition to be checked
	 * @return <code>true</code> if <code>position</code> is the default partition of <code>fDocument</code> or the Lua string partition,
	 *         <code>false</code> otherwise
	 */
	public boolean isAcceptedPartition(ITypedRegion partition) {
		return IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType()) || ILuaPartitions.LUA_STRING.equals(partition.getType())
				|| ILuaPartitions.LUA_SINGLE_QUOTE_STRING.equals(partition.getType())
				|| ILuaPartitions.LUA_MULTI_LINE_STRING.equals(partition.getType());

	}

	/**
	 * Returns the partition at <code>position</code>.
	 * 
	 * @param position
	 *            the position to get the partition for
	 * @return the partition at <code>position</code> or a dummy zero-length partition if accessing the document fails
	 */
	private ITypedRegion getPartition(int position) {
		if (!contains(fCachedPartition, position)) {
			Assert.isTrue(position >= 0);
			Assert.isTrue(position <= fDocument.getLength());

			try {
				fCachedPartition = TextUtilities.getPartition(fDocument, ILuaPartitions.LUA_PARTITIONING, position, false);
			} catch (BadLocationException e) {
				fCachedPartition = new TypedRegion(position, 0, "__no_partition_at_all"); //$NON-NLS-1$
			}
		}

		return fCachedPartition;
	}

	/**
	 * Returns <code>true</code> if <code>region</code> contains <code>position</code>.
	 * 
	 * @param region
	 *            a region
	 * @param position
	 *            an offset
	 * @return <code>true</code> if <code>region</code> contains <code>position</code>
	 */
	private boolean contains(IRegion region, int position) {
		int offset = region.getOffset();
		return offset <= position && position < offset + region.getLength();
	}

	public boolean eatForwardBetweenSepartor(int start, int bound, int leftSeparatorToken, int rightSeparatorToken) {
		int position = start;

		// first token should be the left separator
		int currenttoken = nextToken(position, bound);
		if (currenttoken != leftSeparatorToken)
			return false;

		// eat everything until we found the right separator
		position = getPosition();
		do {
			currenttoken = nextToken(position, bound);

			// if we found the right separator, so we eat all between the 2 separators
			if (currenttoken == rightSeparatorToken) {
				return true;
			}
			// if this is a left separator we must eat if completely
			else if (currenttoken == leftSeparatorToken) {
				// if we don't eat it correctly return false;
				if (!eatForwardBetweenSepartor(position, bound, leftSeparatorToken, rightSeparatorToken))
					return false;
				// else we continue to search ending separator
			} else if (currenttoken == LuaSymbols.TOKEN_EOF) {
				// End of File we stop the search, we can't not eat the pattern completely.
				return false;
			}
			position = getPosition();
		} while (true);
	}

	public boolean eatBackwardBetweenSepartor(int start, int bound, int leftSeparatorToken, int rightSeparatorToken) {
		int position = start;

		// first token should be the right separator
		int currenttoken = previousToken(position, bound);
		if (currenttoken != rightSeparatorToken)
			return false;

		// eat everything until we found the left separator
		position = getPosition();
		do {
			currenttoken = previousToken(position, bound);

			// if we found the left separator, so we eat all between the 2 separators
			if (currenttoken == leftSeparatorToken) {
				return true;
			}
			// if this is a right separator we must eat if completely
			else if (currenttoken == rightSeparatorToken) {
				// if we don't eat it correctly return false;
				if (!eatBackwardBetweenSepartor(position, bound, leftSeparatorToken, rightSeparatorToken))
					return false;
				// else we continue to search ending separator
			} else if (currenttoken == LuaSymbols.TOKEN_EOF) {
				// End of File we stop the search, we can't not eat the pattern completely.
				return false;
			}
			position = getPosition();
		} while (true);
	}

	/**
	 * try to guess if cursor is in an expression
	 * 
	 * @throws BadLocationException
	 */
	public LuaExpression guessLuaExpression(int start) {
		try {

			final int tokenStuffBetweenParentheses = -2000;
			final int tokenStuffBetweenSquareBracket = -2001;

			// find begin of expression
			// ----------------------------------
			int position = start - 1;
			boolean stop = false;

			// eat first token
			int previousToken = previousToken(position, UNBOUND);
			if (previousToken == LuaSymbols.TOKEN_RPAREN) {
				if (!eatBackwardBetweenSepartor(position, UNBOUND, LuaSymbols.TOKEN_LPAREN, LuaSymbols.TOKEN_RPAREN)) {
					return null;
				} else {
					previousToken = tokenStuffBetweenParentheses;
				}
			} else if (previousToken == LuaSymbols.TOKEN_RBRACKET) {
				if (!eatBackwardBetweenSepartor(position, UNBOUND, LuaSymbols.TOKEN_LBRACKET, LuaSymbols.TOKEN_RBRACKET)) {
					return null;
				} else {
					previousToken = tokenStuffBetweenSquareBracket;
				}
			}
			position = getPosition();

			do {
				// get previous token
				int currenttoken = previousToken(position, UNBOUND);

				switch (previousToken) {
				case LuaSymbols.TOKEN_COLON:
				case LuaSymbols.TOKEN_DOT:
				case tokenStuffBetweenParentheses:
				case tokenStuffBetweenSquareBracket:
					// manage case where there are 2 dots
					if (previousToken == LuaSymbols.TOKEN_DOT && currenttoken == LuaSymbols.TOKEN_DOT) {
						position = position + 1;
						stop = true;
						break;
					}

					// before dot and colon or left parent, we must found '(...)' or an identifier
					if (currenttoken != LuaSymbols.TOKEN_RPAREN && currenttoken != LuaSymbols.TOKEN_IDENT
							&& currenttoken != LuaSymbols.TOKEN_RBRACKET)
						return null;

					if (currenttoken == LuaSymbols.TOKEN_RPAREN) {
						// case : stuff between parentheses
						if (!eatBackwardBetweenSepartor(position, UNBOUND, LuaSymbols.TOKEN_LPAREN, LuaSymbols.TOKEN_RPAREN)) {
							return null;
						} else {
							previousToken = tokenStuffBetweenParentheses;
						}
					} else if (currenttoken == LuaSymbols.TOKEN_RBRACKET) {
						// case : stuff between parentheses
						if (!eatBackwardBetweenSepartor(position, UNBOUND, LuaSymbols.TOKEN_LBRACKET, LuaSymbols.TOKEN_RBRACKET)) {
							return null;
						} else {
							previousToken = tokenStuffBetweenSquareBracket;
						}
					} else {
						// case : identifier
						previousToken = currenttoken;
					}
					position = getPosition();
					break;
				case LuaSymbols.TOKEN_IDENT:
					// before an identifier, we must found dot or colon
					if (currenttoken == LuaSymbols.TOKEN_COLON || currenttoken == LuaSymbols.TOKEN_DOT) {
						position = getPosition();
						previousToken = currenttoken;
					} else {
						stop = true;
					}
					break;
				default:
					return null;
				}

			} while (!stop);

			// parse expression
			// ----------------------------------
			position = position + 1;
			int nextToken = nextToken(position, start);

			// 1 - first token must be an identifier
			if (nextToken != LuaSymbols.TOKEN_IDENT)
				return null;
			String itemname = fDocument.get(position, getPosition() - position).trim();
			Item item = new UnknownItem();
			item.setName(itemname);
			Identifier identifier = new Identifier();
			identifier.setDefinition(item);
			identifier.setStart(position);
			identifier.setEnd(getPosition());

			// 2 - next token should be expression (index, call, invoke)
			stop = false;
			position = getPosition();
			LuaExpression exp = identifier;
			do {
				nextToken = nextToken(position, start);
				switch (nextToken) {
				case LuaSymbols.TOKEN_EOF:
					stop = true;
					break;
				case LuaSymbols.TOKEN_LBRACKET:
					// create index
					Index squareIndex = new Index();
					squareIndex.setLeft(exp);

					// if this is a string we try to find the right value of the index.
					int contentbegin = getPosition();
					nextToken = nextToken(getPosition(), start);
					if (nextToken == LuaSymbols.TOKEN_STRING) {
						String fieldname = fDocument.get(contentbegin, getPosition() - contentbegin).trim();
						if (fieldname != null) {
							if (fieldname.startsWith("\"") && fieldname.endsWith("\"")) {//$NON-NLS-1$//$NON-NLS-2$
								fieldname = fieldname.replaceAll("^\"(.*)\"$", "$1"); //$NON-NLS-1$//$NON-NLS-2$
							} else if (fieldname.startsWith("'") && fieldname.endsWith("'")) {//$NON-NLS-1$//$NON-NLS-2$
								fieldname = fieldname.replaceAll("^'(.*)'$", "$1"); //$NON-NLS-1$//$NON-NLS-2$
							} else {
								fieldname = null;
							}
							squareIndex.setRight(fieldname);
						}
					}

					// eat bracket
					eatForwardBetweenSepartor(position, start, LuaSymbols.TOKEN_LBRACKET, LuaSymbols.TOKEN_RBRACKET);

					// update loop variable
					exp = squareIndex;
					position = getPosition();
					break;
				case LuaSymbols.TOKEN_DOT:
					// manage dot index
					position = getPosition();
					nextToken = nextToken(getPosition(), start);

					// next token should be an identifier or EOF for incomplete index
					String indexfieldname;
					if (nextToken == LuaSymbols.TOKEN_EOF) {
						indexfieldname = ""; //$NON-NLS-1$
					} else if (nextToken == LuaSymbols.TOKEN_IDENT) {
						indexfieldname = fDocument.get(position, getPosition() - position).trim();
					} else {
						// if it's not an identifier or a EOF, we do not manage this case
						return null;
					}

					// create index
					Index index = new Index();
					index.setLeft(exp);
					index.setRight(indexfieldname);
					if (nextToken == LuaSymbols.TOKEN_EOF)
						index.setIncomplete(true); // no identifier after the '.' this is an incomplete Index

					// update loop variable
					exp = index;
					position = getPosition();
					break;
				case LuaSymbols.TOKEN_COLON:
					// manage invoke
					position = getPosition();

					// next token should be an identifier or EOF for incomplete invocation
					nextToken = nextToken(getPosition(), start);
					String invokefunctionname;
					if (nextToken == LuaSymbols.TOKEN_EOF) {
						invokefunctionname = ""; //$NON-NLS-1$
					} else if (nextToken == LuaSymbols.TOKEN_IDENT) {
						invokefunctionname = fDocument.get(position, getPosition() - position).trim();
					} else {
						// if it's not an identifier or a EOF, we do not manage this case
						return null;
					}
					// create Invoke
					Invoke invoke = new Invoke();
					invoke.setRecord(exp);
					invoke.setFunctionName(invokefunctionname);

					// consume the full invocation ( left parent until right parent)
					position = getPosition();
					nextToken = nextToken(getPosition(), start);
					if (nextToken == LuaSymbols.TOKEN_LPAREN) {
						if (!eatForwardBetweenSepartor(position, start, LuaSymbols.TOKEN_LPAREN, LuaSymbols.TOKEN_RPAREN))
							invoke.setIncomplete(true); // no right parentheses so this is an incomplete invoke
					} else if (nextToken != LuaSymbols.TOKEN_EOF) {
						return null; // invalid invocation
					} else {
						invoke.setIncomplete(true); // no left parentheses so this is an incomplete invoke
					}

					// update loop variable
					exp = invoke;
					position = getPosition();
					break;
				case LuaSymbols.TOKEN_LPAREN:
					// manage call
					Call call = new Call();
					call.setFunction(exp);

					// consume the call invocation ( left parent until right parent)
					if (!eatForwardBetweenSepartor(position, start, LuaSymbols.TOKEN_LPAREN, LuaSymbols.TOKEN_RPAREN)) {
						call.setIncomplete(true); // no right parentheses so this is an incomplete invoke
					}

					// update loop variable
					exp = call;
					position = getPosition();
					break;
				default:
					return null;
				}
			} while (!stop);

			return exp;
		} catch (BadLocationException e) {
		}
		return null;
	}
}
