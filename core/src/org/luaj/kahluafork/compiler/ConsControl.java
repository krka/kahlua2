package org.luaj.kahluafork.compiler;

public class ConsControl {
	expdesc v = new expdesc(); /* last list item read */
	expdesc t; /* table descriptor */
	int nh; /* total number of `record' elements */
	int na; /* total number of array elements */
	int tostore; /* number of array elements pending to be stored */
}
