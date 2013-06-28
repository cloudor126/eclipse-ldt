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

/**
 * Utility methods for heuristic based Lua manipulations in an incomplete Lua source file.
 * 
 * <p>
 * An instance holds some internal position in the document and is therefore not threadsafe.
 * </p>
 * 
 * inspired by org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner
 */
public class LuaHeuristicScanner implements LuaSymbols {

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
	private static final char LPAREN = '(';
	private static final char RPAREN = ')';
	private static final char SEMICOLON = ';';
	private static final char COLON = ':';
	private static final char COMMA = ',';
	private static final char LBRACKET = '[';
	private static final char RBRACKET = ']';
	private static final char EQUAL = '=';
	private static final char LANGLE = '<';
	private static final char RANGLE = '>';
	private static final char PLUS = '+';
	private static final char MINUS = '-';
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
			return TOKEN_EOF;

		fPos++;

		// first, detect string
		if (getPartition(pos).getType().equals(ILuaPartitions.LUA_STRING)) {
			return TOKEN_STRING;
		}

		// detect key char
		switch (fChar) {
		case LBRACE:
			return TOKEN_LBRACE;
		case RBRACE:
			return TOKEN_RBRACE;
		case LBRACKET:
			return TOKEN_LBRACKET;
		case RBRACKET:
			return TOKEN_RBRACKET;
		case LPAREN:
			return TOKEN_LPAREN;
		case RPAREN:
			return TOKEN_RPAREN;
		case SEMICOLON:
			return TOKEN_SEMICOLON;
		case COMMA:
			return TOKEN_COMMA;
		case EQUAL:
			return TOKEN_EQUAL;
		case LANGLE:
			return TOKEN_LESSTHAN;
		case RANGLE:
			return TOKEN_GREATERTHAN;
		case PLUS:
			return TOKEN_PLUS;
		case MINUS:
			return TOKEN_MINUS;
		case SHARP:
			return TOKEN_SHARP;
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
				return TOKEN_EOF;
			}

			return getToken(identOrKeyword);

		} else {
			// operators, number literals etc
			return TOKEN_OTHER;
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
			return TOKEN_EOF;

		fPos--;

		// first, detect string
		if (getPartition(pos).getType().equals(ILuaPartitions.LUA_STRING)) {
			return TOKEN_STRING;
		}

		// detect key char
		switch (fChar) {
		case LBRACE:
			return TOKEN_LBRACE;
		case RBRACE:
			return TOKEN_RBRACE;
		case LBRACKET:
			return TOKEN_LBRACKET;
		case RBRACKET:
			return TOKEN_RBRACKET;
		case LPAREN:
			return TOKEN_LPAREN;
		case RPAREN:
			return TOKEN_RPAREN;
		case SEMICOLON:
			return TOKEN_SEMICOLON;
		case COLON:
			return TOKEN_COLON;
		case COMMA:
			return TOKEN_COMMA;
		case EQUAL:
			return TOKEN_EQUAL;
		case LANGLE:
			return TOKEN_LESSTHAN;
		case RANGLE:
			return TOKEN_GREATERTHAN;
		case PLUS:
			return TOKEN_PLUS;
		case MINUS:
			return TOKEN_MINUS;
		case SHARP:
			return TOKEN_SHARP;
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
				return TOKEN_EOF;
			}

			return getToken(identOrKeyword);

		} else {
			// operators, number literals etc
			return TOKEN_OTHER;
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
				return TOKEN_IF;
			if ("do".equals(s)) //$NON-NLS-1$
				return TOKEN_DO;
			if ("or".equals(s)) //$NON-NLS-1$
				return TOKEN_OR;
			break;
		case 3:
			if ("for".equals(s)) //$NON-NLS-1$
				return TOKEN_FOR;
			if ("end".equals(s)) //$NON-NLS-1$
				return TOKEN_END;
			if ("nil".equals(s)) //$NON-NLS-1$
				return TOKEN_NIL;
			if ("and".equals(s)) //$NON-NLS-1$
				return TOKEN_AND;
			if ("not".equals(s)) //$NON-NLS-1$
				return TOKEN_NOT;
			break;
		case 4:
			if ("else".equals(s)) //$NON-NLS-1$
				return TOKEN_ELSE;
			if ("then".equals(s)) //$NON-NLS-1$
				return TOKEN_THEN;
			if ("true".equals(s)) //$NON-NLS-1$
				return TOKEN_TRUE;
			break;
		case 5:
			if ("break".equals(s)) //$NON-NLS-1$
				return TOKEN_BREAK;
			if ("while".equals(s)) //$NON-NLS-1$
				return TOKEN_WHILE;
			if ("local".equals(s)) //$NON-NLS-1$
				return TOKEN_LOCAL;
			if ("false".equals(s)) //$NON-NLS-1$
				return TOKEN_FALSE;
			break;
		case 6:
			if ("return".equals(s)) //$NON-NLS-1$
				return TOKEN_RETURN;
			if ("repeat".equals(s)) //$NON-NLS-1$
				return TOKEN_REPEAT;
			if ("elseif".equals(s)) //$NON-NLS-1$
				return TOKEN_ELSEIF;
			break;
		case 8:
			if ("function".equals(s)) //$NON-NLS-1$
				return TOKEN_FUNCTION;
			break;
		default:
			break;
		}
		return TOKEN_IDENT;
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
		return IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType()) || ILuaPartitions.LUA_STRING.equals(partition.getType());

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
}
