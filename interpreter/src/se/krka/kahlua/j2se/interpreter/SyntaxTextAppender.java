/*
 Copyright (c) 2010 Kristofer Karlsson <kristofer.karlsson@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package se.krka.kahlua.j2se.interpreter;

import jsyntaxpane.Lexer;
import jsyntaxpane.Token;

import javax.swing.JScrollBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

class SyntaxTextAppender implements Runnable {
	private final String text;
	private final Lexer lexer;
	private final OutputTerminal outputTerminal;
	private final CountDownLatch latch = new CountDownLatch(1);

	public SyntaxTextAppender(OutputTerminal outputTerminal, String text, Lexer lexer) {
		this.outputTerminal = outputTerminal;
		this.text = text;
		this.lexer = lexer;
	}

	@Override
	public void run() {
		JScrollBar vert = outputTerminal.scrollpane.getVerticalScrollBar();
		boolean isAtBottom = vert.getValue() + vert.getVisibleAmount() >= vert.getMaximum() - 32;
		outputTerminal.scrollDown = !vert.getValueIsAdjusting() && isAtBottom;

		try {
			Document document = outputTerminal.editorPane.getDocument();
			int startPos = document.getLength();

			Segment insertSegment = new Segment(text.toCharArray(), 0, text.length());
			ArrayList<Token> newTokens = new ArrayList<Token>();
			lexer.parse(insertSegment, 0, newTokens);
			outputTerminal.voidLexer.setNewTokens(newTokens, document.getLength());

			document.insertString(startPos, text, null);

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		latch.countDown();
	}

	public void await() throws InterruptedException {
		latch.await();
	}
}
