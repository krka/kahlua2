package se.krka.kahlua.j2se.interpreter;

import jsyntaxpane.Lexer;
import jsyntaxpane.Token;

import javax.swing.JScrollBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import java.util.ArrayList;

/**
* Created by IntelliJ IDEA.
* User: krka
* Date: 2010-jun-22
* Time: 17:25:16
* To change this template use File | Settings | File Templates.
*/
class SyntaxTextAppender implements Runnable {
	private final String text;
	private final Lexer lexer;
	private OutputTerminal outputTerminal;

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
	}
}
