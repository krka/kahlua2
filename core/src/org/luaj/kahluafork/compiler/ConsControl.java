package org.luaj.kahluafork.compiler;

/**
 * @exclude
 */
public class ConsControl {
	ExpDesc v = new ExpDesc(); /* last list item read */
	ExpDesc t; /* table descriptor */
	int nh; /* total number of `record' elements */
	int na; /* total number of array elements */
	int tostore; /* number of array elements pending to be stored */
}
