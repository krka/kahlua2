package org.luaj.kahluafork.compiler;

/*
** structure to chain all variables in the left-hand side of an
** assignment
*/
public class LHS_assign {
	LHS_assign prev;
	/* variable (global, local, upvalue, or indexed) */
	ExpDesc v = new ExpDesc();
}
