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

import jsyntaxpane.*;
import jsyntaxpane.lexers.LuaLexer;
import se.krka.kahlua.j2se.interpreter.jsyntax.JSyntaxUtil;
import se.krka.kahlua.j2se.interpreter.jsyntax.KahluaKit;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class OutputTerminal extends JPanel implements FocusListener {
	private final AppenderThread appender;

    final JScrollPane scrollpane;
    boolean scrollDown;
    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(1, 5, 1, 5);

	final JEditorPane editorPane;
    final VoidLexer voidLexer;

    private final PrintStream printStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
			appender.append((char) b);
        }
    });

    private Lexer luaLexer = new LuaLexer();
    private Lexer errorLexer = new TypeLexer(TokenType.ERROR);
    private Lexer outputLexer = new TypeLexer(null);
    private Lexer infoLexer = new TypeLexer(TokenType.WARNING);

    public OutputTerminal(Color background, JComponent input) {
        super(new BorderLayout());

        editorPane = new JEditorPane();
        voidLexer = new VoidLexer();

		appender = new AppenderThread(outputLexer, this);

        JSyntaxUtil.installSyntax(editorPane, false, new KahluaKit(voidLexer));
        voidLexer.doc = (SyntaxDocument) editorPane.getDocument();
        
        editorPane.setBackground(background);
        editorPane.setBorder(EMPTY_BORDER);
        editorPane.setEditable(false);
        editorPane.setFocusable(true);
        editorPane.addFocusListener(this);

        scrollpane = new JScrollPane(
                filler(editorPane, input),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollpane.getVerticalScrollBar().setUnitIncrement(20);
        scrollpane.getVerticalScrollBar().setBlockIncrement(200);
        scrollpane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (scrollDown) {
                    e.getAdjustable().setValue(e.getAdjustable().getMaximum());
                    scrollDown = false;
                }
            }
        });

        setFocusable(true);
        add(scrollpane, BorderLayout.CENTER);
	}

    public synchronized void appendLua(String text) {
        append(text, luaLexer);
    }

    public synchronized void appendError(String text) {
        append(text, errorLexer);
    }

    public synchronized void appendInfo(String text) {
        append(text, infoLexer);
    }

    private synchronized void append(final String text, final Lexer lexer) {
		SwingUtilities.invokeLater(new SyntaxTextAppender(this, text, lexer));
    }

    public PrintStream getPrintStream() {
        return printStream;
    }


    @Override
    public void focusGained(FocusEvent e) {
        this.requestFocus();
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    private static JPanel filler(JComponent other, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel glue = new JPanel();
        glue.setOpaque(true);
        glue.setBackground(other.getBackground());
        panel.add(glue, BorderLayout.CENTER);
        panel.add(other, BorderLayout.SOUTH);

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(panel, BorderLayout.CENTER);
        panel2.add(input, BorderLayout.SOUTH);
        return panel2;
    }


	public void appendOutput(String s) {
		appender.append(s);
	}
}
