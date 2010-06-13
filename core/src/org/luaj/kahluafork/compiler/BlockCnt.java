package org.luaj.kahluafork.compiler;

/**
 * @exclude
 */
public class BlockCnt {
	BlockCnt previous;  /* chain */
	int breaklist;  /* list of jumps out of this loop */
	int nactvar;  /* # active locals outside the breakable structure */
	boolean upval;  /* true if some variable in the block is an upvalue */
	boolean isbreakable;  /* true if `block' is a loop */
}
