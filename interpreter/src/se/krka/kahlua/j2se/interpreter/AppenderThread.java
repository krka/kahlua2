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

import javax.swing.SwingUtilities;

public class AppenderThread {
	private final StringBuilder buffer = new StringBuilder();
	private final OutputTerminal outputTerminal;
	private final Lexer lexer;

	private final Thread worker = new Thread() {
		@Override
		public void run() {
			long runTime = 1;
			try {
				while (true) {
					if (buffer.length() > 0) {
						String text;
						synchronized (buffer) {
							text = buffer.toString();
							buffer.setLength(0);
						}
						if (text.length() > 0) {
							long t1 = System.currentTimeMillis();
							SyntaxTextAppender appender = new SyntaxTextAppender(outputTerminal, text, lexer);
							SwingUtilities.invokeLater(appender);
							appender.await();
							long t2 = System.currentTimeMillis();
							runTime = Math.min(1, t2 - t1);
						}
					}
					Thread.sleep(10 * runTime);
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	};

	public AppenderThread(Lexer lexer, OutputTerminal outputTerminal) {
		this.lexer = lexer;
		this.outputTerminal = outputTerminal;
		worker.setDaemon(false);
		worker.start();
	}

	public void append(char c) {
		synchronized (buffer) {
			buffer.append(c);
		}
	}

	public void append(String s) {
		synchronized (buffer) {
			buffer.append(s);
		}
	}
}
