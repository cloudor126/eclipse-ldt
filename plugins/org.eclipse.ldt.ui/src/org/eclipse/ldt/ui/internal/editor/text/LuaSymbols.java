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
package org.eclipse.ldt.ui.internal.editor.text;

/**
 * Symbols for the heuristic Lua scanner.
 * 
 * inspired by org.eclipse.jdt.internal.ui.text.Symbols
 */
public final class LuaSymbols {

	private LuaSymbols() {
	}

	// CHECKSTYLE:OFF
	public static final int TOKEN_OTHER = -2;
	public static final int TOKEN_EOF = -1;

	// separators
	public static final int TOKEN_LBRACE = 1; // {
	public static final int TOKEN_RBRACE = 2; // }
	public static final int TOKEN_LBRACKET = 3; // [
	public static final int TOKEN_RBRACKET = 4; // ]
	public static final int TOKEN_LPAREN = 5; // (
	public static final int TOKEN_RPAREN = 6; // )

	public static final int TOKEN_SEMICOLON = 7; // ;
	public static final int TOKEN_COMMA = 9; // ,

	// operators
	public static final int TOKEN_COLON = 10; // :
	public static final int TOKEN_DOT = 11; // .

	public static final int TOKEN_EQUAL = 12; // =
	public static final int TOKEN_LESSTHAN = 13; // <
	public static final int TOKEN_GREATERTHAN = 14; // >

	public static final int TOKEN_PLUS = 15; // +
	public static final int TOKEN_MINUS = 16; // -
	public static final int TOKEN_ASTERISK = 17; // *
	public static final int TOKEN_SLASH = 18; // /
	public static final int TOKEN_PERSENT = 19; // %
	public static final int TOKEN_CARET = 20; // ^

	public static final int TOKEN_SHARP = 21; // #

	// keywords/operators
	public static final int TOKEN_OR = 1031;
	public static final int TOKEN_AND = 1032;
	public static final int TOKEN_NOT = 1033;

	// keywords
	public static final int TOKEN_IF = 1009;
	public static final int TOKEN_DO = 1010;
	public static final int TOKEN_FOR = 1011;
	public static final int TOKEN_REPEAT = 1012;
	public static final int TOKEN_ELSE = 1013;
	public static final int TOKEN_ELSEIF = 1014;
	public static final int TOKEN_BREAK = 1015;
	public static final int TOKEN_WHILE = 1016;
	public static final int TOKEN_RETURN = 1017;
	public static final int TOKEN_THEN = 1018;
	public static final int TOKEN_END = 1019;
	public static final int TOKEN_LOCAL = 1020;
	public static final int TOKEN_FUNCTION = 1021;

	public static final int TOKEN_TRUE = 1022;
	public static final int TOKEN_FALSE = 1023;
	public static final int TOKEN_NIL = 1024;

	// others
	public static final int TOKEN_IDENT = 2000;
	public static final int TOKEN_STRING = 3000;

	// CHECKSTYLE:ON

	public static boolean isKeywords(int token) {
		return (token / 1000) == 1;
	}
}