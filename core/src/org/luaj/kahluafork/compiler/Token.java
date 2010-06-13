package org.luaj.kahluafork.compiler;

/**
 * @exclude
 */
public class Token {
	int token;

	/* semantics information */
	double r;
	String ts;

	public void set(Token other) {
		this.token = other.token;
		this.r = other.r;
		this.ts = other.ts;
	}
}
