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

/**
 * Symbols for the heuristic Lua scanner.
 * 
 * inspired by org.eclipse.jdt.internal.ui.text.Symbols
 */
public interface LuaSymbols {
	int TOKEN_EOF = -1;
	int TOKEN_LBRACE = 1; // {
	int TOKEN_RBRACE = 2; // }
	int TOKEN_LBRACKET = 3; // [
	int TOKEN_RBRACKET = 4; // ]
	int TOKEN_LPAREN = 5; // (
	int TOKEN_RPAREN = 6; // )
	int TOKEN_SEMICOLON = 7; // ;

	int TOKEN_OTHER = 8;

	int TOKEN_COLON = 9; // :
	int TOKEN_COMMA = 10; // ,

	int TOKEN_EQUAL = 11; // =
	int TOKEN_LESSTHAN = 12; // <
	int TOKEN_GREATERTHAN = 13; // >

	int TOKEN_PLUS = 14; // +
	int TOKEN_MINUS = 15; // -
	int TOKEN_ASTERISK = 16; // *
	int TOKEN_SLASH = 17; // /
	int TOKEN_PERSENT = 18; // %
	int TOKEN_CARET = 19; // ^

	int TOKEN_SHARP = 20; // #

	int TOKEN_DOT = 21; // .
	int TOKEN_DOUBLEDOT = 22;// :

	int TOKEN_OR = 23;
	int TOKEN_AND = 24;
	int TOKEN_NOT = 25;

	int TOKEN_STRING = 26;

	int TOKEN_IF = 1009;
	int TOKEN_DO = 1010;
	int TOKEN_FOR = 1011;
	int TOKEN_REPEAT = 1012;
	int TOKEN_ELSE = 1013;
	int TOKEN_ELSEIF = 1014;
	int TOKEN_BREAK = 1015;
	int TOKEN_WHILE = 1016;
	int TOKEN_RETURN = 1017;
	int TOKEN_THEN = 1018;
	int TOKEN_END = 1019;
	int TOKEN_LOCAL = 1020;
	int TOKEN_FUNCTION = 1021;

	int TOKEN_TRUE = 1022;
	int TOKEN_FALSE = 1023;
	int TOKEN_NIL = 1024;

	int TOKEN_IDENT = 2000;
}