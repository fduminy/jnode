/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */



package sun.tools.javap;

/**
 * This interface defines constant that are used
 * throughout the compiler. It inherits from RuntimeConstants,
 * which is an autogenerated class that contains contstants
 * defined in the interpreter.
 */

public
interface Constants extends RuntimeConstants {

     /**
     * End of input
     */
    public static final int EOF	= -1;

   /*
     * Flags
     */
    public static final int F_VERBOSE		= 1 << 0;
    public static final int F_DUMP		= 1 << 1;
    public static final int F_WARNINGS		= 1 << 2;
    public static final int F_DEBUG		= 1 << 3;
    public static final int F_OPTIMIZE		= 1 << 4;
    public static final int F_DEPENDENCIES	= 1 << 5;

    /*
     * Type codes
     */
    public static final int TC_BOOLEAN   = 0;
    public static final int TC_BYTE   	 = 1;
    public static final int TC_CHAR      = 2;
    public static final int TC_SHORT     = 3;
    public static final int TC_INT       = 4;
    public static final int TC_LONG      = 5;
    public static final int TC_FLOAT     = 6;
    public static final int TC_DOUBLE    = 7;
    public static final int TC_NULL      = 8;
    public static final int TC_ARRAY     = 9;
    public static final int TC_CLASS     = 10;
    public static final int TC_VOID      = 11;
    public static final int TC_METHOD    = 12;
    public static final int TC_ERROR     = 13;

    /*
     * Type Masks
     */
    public static final int TM_NULL      = 1 << TC_NULL;
    public static final int TM_VOID      = 1 << TC_VOID;
    public static final int TM_BOOLEAN   = 1 << TC_BOOLEAN;
    public static final int TM_BYTE      = 1 << TC_BYTE;
    public static final int TM_CHAR      = 1 << TC_CHAR;
    public static final int TM_SHORT     = 1 << TC_SHORT;
    public static final int TM_INT       = 1 << TC_INT;
    public static final int TM_LONG      = 1 << TC_LONG;
    public static final int TM_FLOAT     = 1 << TC_FLOAT;
    public static final int TM_DOUBLE    = 1 << TC_DOUBLE;
    public static final int TM_ARRAY     = 1 << TC_ARRAY;
    public static final int TM_CLASS     = 1 << TC_CLASS;
    public static final int TM_METHOD    = 1 << TC_METHOD;
    public static final int TM_ERROR     = 1 << TC_ERROR;

    public static final int TM_INT32     = TM_BYTE | TM_SHORT | TM_CHAR | TM_INT;
    public static final int TM_NUM32     = TM_INT32 | TM_FLOAT;
    public static final int TM_NUM64     = TM_LONG | TM_DOUBLE;
    public static final int TM_INTEGER   = TM_INT32 | TM_LONG;
    public static final int TM_REAL      = TM_FLOAT | TM_DOUBLE;
    public static final int TM_NUMBER    = TM_INTEGER | TM_REAL;
    public static final int TM_REFERENCE = TM_ARRAY | TM_CLASS | TM_NULL;

    /*
     * Class status
     */
    public static final int CS_UNDEFINED	= 0;
    public static final int CS_UNDECIDED	= 1;
    public static final int CS_BINARY		= 2;
    public static final int CS_SOURCE		= 3;
    public static final int CS_PARSED		= 4;
    public static final int CS_COMPILED		= 5;
    public static final int CS_NOTFOUND		= 6;

    /*
     * Attributes
     */
    public static final int ATT_ALL		= -1;
    public static final int ATT_CODE		= 1;

    /*
     * Number of bits used in file offsets
     */
    public static final int OFFSETBITS 		= 19;
    public static final int MAXFILESIZE		= (1 << OFFSETBITS) - 1;
    public static final int MAXLINENUMBER	= (1 << (32 - OFFSETBITS)) - 1;

    /*
     * Operators
     */
    public final int COMMA		= 0;
    public final int ASSIGN		= 1;

    public final int ASGMUL		= 2;
    public final int ASGDIV		= 3;
    public final int ASGREM		= 4;
    public final int ASGADD		= 5;
    public final int ASGSUB		= 6;
    public final int ASGLSHIFT		= 7;
    public final int ASGRSHIFT		= 8;
    public final int ASGURSHIFT		= 9;
    public final int ASGBITAND		= 10;
    public final int ASGBITOR	       	= 11;
    public final int ASGBITXOR		= 12;

    public final int COND		= 13;
    public final int OR			= 14;
    public final int AND		= 15;
    public final int BITOR		= 16;
    public final int BITXOR		= 17;
    public final int BITAND		= 18;
    public final int NE			= 19;
    public final int EQ			= 20;
    public final int GE			= 21;
    public final int GT			= 22;
    public final int LE			= 23;
    public final int LT			= 24;
    public final int INSTANCEOF		= 25;
    public final int LSHIFT		= 26;
    public final int RSHIFT		= 27;
    public final int URSHIFT		= 28;
    public final int ADD		= 29;
    public final int SUB		= 30;
    public final int DIV		= 31;
    public final int REM		= 32;
    public final int MUL		= 33;
    public final int CAST		= 34;		// (x)y
    public final int POS		= 35;		// +x
    public final int NEG		= 36;		// -x
    public final int NOT		= 37;
    public final int BITNOT		= 38;
    public final int PREINC		= 39;		// ++x
    public final int PREDEC		= 40; 		// --x
    public final int NEWARRAY		= 41;
    public final int NEWINSTANCE	= 42;
    public final int NEWFROMNAME	= 43;
    public final int POSTINC		= 44;		// x++
    public final int POSTDEC		= 45;		// x--
    public final int FIELD		= 46;
    public final int METHOD		= 47;		// x(y)
    public final int ARRAYACCESS	= 48;		// x[y]
    public final int NEW		= 49;
    public final int INC		= 50;
    public final int DEC		= 51;

    public final int CONVERT		= 55;		// implicit conversion
    public final int EXPR		= 56;		// (x)
    public final int ARRAY		= 57;		// {x, y, ...}
    public final int GOTO		= 58;

    /*
     * Value tokens
     */
    public final int IDENT		= 60;
    public final int BOOLEANVAL		= 61;
    public final int BYTEVAL		= 62;
    public final int CHARVAL		= 63;
    public final int SHORTVAL		= 64;
    public final int INTVAL			= 65;
    public final int LONGVAL		= 66;
    public final int FLOATVAL		= 67;
    public final int DOUBLEVAL		= 68;
    public final int STRINGVAL		= 69;

    /*
     * Type keywords
     */
    public final int BYTE		= 70;
    public final int CHAR		= 71;
    public final int SHORT		= 72;
    public final int INT		= 73;
    public final int LONG		= 74;
    public final int FLOAT		= 75;
    public final int DOUBLE		= 76;
    public final int VOID		= 77;
    public final int BOOLEAN		= 78;

    /*
     * Expression keywords
     */
    public final int TRUE		= 80;
    public final int FALSE		= 81;
    public final int THIS		= 82;
    public final int SUPER		= 83;
    public final int NULL		= 84;

    /*
     * Statement keywords
     */
    public final int IF			= 90;
    public final int ELSE		= 91;
    public final int FOR		= 92;
    public final int WHILE		= 93;
    public final int DO			= 94;
    public final int SWITCH		= 95;
    public final int CASE		= 96;
    public final int DEFAULT		= 97;
    public final int BREAK		= 98;
    public final int CONTINUE		= 99;
    public final int RETURN		= 100;
    public final int TRY		= 101;
    public final int CATCH		= 102;
    public final int FINALLY		= 103;
    public final int THROW		= 104;
    public final int STAT		= 105;
    public final int EXPRESSION		= 106;
    public final int DECLARATION	= 107;
    public final int VARDECLARATION	= 108;

    /*
     * Declaration keywords
     */
    public final int IMPORT		= 110;
    public final int CLASS		= 111;
    public final int EXTENDS		= 112;
    public final int IMPLEMENTS		= 113;
    public final int INTERFACE		= 114;
    public final int PACKAGE		= 115;

    /*
     * Modifier keywords
     */
    public final int PRIVATE	= 120;
    public final int PUBLIC		= 121;
    public final int PROTECTED	= 122;
    public final int CONST 		= 123;
    public final int STATIC		= 124;
    public final int TRANSIENT		= 125;
    public final int SYNCHRONIZED	= 126;
    public final int NATIVE		= 127;
    public final int FINAL		= 128;
    public final int VOLATILE	= 129;
    public final int ABSTRACT	= 130;
    public final int STRICT		= 165;

    /*
     * Punctuation
     */
    public final int SEMICOLON	= 135;
    public final int COLON		= 136;
    public final int QUESTIONMARK  	= 137;
    public final int LBRACE		= 138;
    public final int RBRACE		= 139;
    public final int LPAREN		= 140;
    public final int RPAREN		= 141;
    public final int LSQBRACKET	= 142;
    public final int RSQBRACKET	= 143;
    public final int THROWS     = 144;

    /*
     * Special tokens
     */
    public final int ERROR		= 145;		// an error 
    public final int COMMENT	= 146;          // not used anymore.
    public final int TYPE		= 147;
    public final int LENGTH		= 148;
    public final int INLINERETURN	= 149;
    public final int INLINEMETHOD	= 150;
    public final int INLINENEWINSTANCE	= 151;

    /*
     * Added for jasm
     */
	public final int METHODREF	= 152;
	public final int FIELDREF	= 153;
    public final int STACK		= 154;
    public final int LOCAL		= 155;
    public final int CPINDEX	= 156;
    public final int CPNAME		= 157;
    public final int SIGN		= 158;
    public final int BITS		= 159;
    public final int INF		= 160;
    public final int NAN		= 161;
    public final int INNERCLASS	= 162;
    public final int OF		= 163;
    public final int SYNTHETIC		= 164;
// last used=165;

   /*
     * Operator precedence
     */
    public static final int opPrecedence[] = {
	10,	11,	11,	11,	11,	11,	11,	11,	11,	11,
	11,	11,	11,	12,	13,	14,	15,	16,	17,	18,
	18,	19,	19,	19,	19,	19,	20,	20,	20,	21,
	21,	22,	22,	22,	23,	24,	24,	24,	24,	24,
	24,	25,	25, 	26,	26,	26,	26,	26,	26
    };

    /*
     * Operator names
     */
    public static final String opNames[] = {
	",",		"=",		"*=",		"/=",		"%=",
	"+=",		"-=",		"<<=",		">>=",		"<<<=",
	"&=",		"|=",		"^=",		"?:",		"||",
	"&&",		"|",		"^",		"&",		"!=",
	"==", 		">=",		">",		"<=",		"<",
	"instanceof",	"<<",		">>",		"<<<",		"+",
	"-",		"/",		"%",		"*",		"cast",
	"+",		"-",		"!",		"~",		"++",
	"--",		"new",		"new",		"new",		"++",
	"--",		"field",	"method",	"[]",		"new",
	"++",		"--",		null,		null,		null,

	"convert",	"expr",		"array",	"goto",		null,		

	"Identifier",	"Boolean",	"Byte",		"Char",		"Short",
	"Integer",		"Long",		"Float",	"Double",	"String",

	"byte",		"char",		"short",	"int",		"long",
	"float",	"double",	"void",		"boolean",	null,

	"true",		"false",	"this",		"super",	"null",
	null,		null,		null,		null,		null,

	"if",		"else",		"for",		"while",	"do",
	"switch",	"case",		"default",	"break",	"continue",
	"return",	"try",		"catch",	"finally",	"throw",
	"stat",		"expression",	"declaration",	"declaration",  null,

	"import",	"class",	"extends",	"implements",	"interface",
	"package",	null,		null,		null,		null,

	"private",	"public",	"protected",	"const",	"static",
	"transient",	"synchronized",	"native",	"final",	"volatile",
	"abstract",	null,		null,		null,		null,

	";",		":",		"?",		"{",		"}",
	"(",		")",		"[",		"]",		"throws",
	"error",	"comment",	"type",		"length",	"inline-return",
	"inline-method", "inline-new",
	"method", "field", "stack", "locals", "CPINDEX", "CPName", "SIGN",
	"bits", "INF", "NaN", "InnerClass", "of", "synthetic"
    };

}



