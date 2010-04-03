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

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Terminal extends JPanel {
    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    private final JScrollBar scrollbar;
    private final Style defaultStyle;

    private final PrintStream printStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            append(String.valueOf((char) b), null);
        }
    });

    public Terminal(boolean editable, Color foreground) {
        super(new BorderLayout());
        textPane = new JTextPane();
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 10));
        setBackground(Color.BLACK);
        setForeground(foreground);
        defaultStyle = createStyle("default", foreground);
        textPane.setAutoscrolls(editable);
        textPane.setEditable(editable);
        textPane.setCaretPosition(0);
        textPane.setCaretColor(Color.WHITE);

        scrollPane = new JScrollPane(
                textPane,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollbar = scrollPane.getVerticalScrollBar();
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void setBackground(Color color) {
        if (textPane != null) {
            textPane.setBackground(color);
        }
    }

    public void setForeground(Color color) {
        if (textPane != null) {
            textPane.setForeground(color);
        }
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public Style createStyle(String name, Color color) {
        Style style = textPane.addStyle(name, null);
        StyleConstants.setForeground(style, color);
        return style;
    }

    public void appendLine(String s, Style style) {
        if (!s.endsWith("\n")) {
            s = s + "\n";
        }
        append(s, style);
    }

	public void append(String text, Style style) {
        if (style == null) {
            style = defaultStyle;
        }
        StyledDocument doc = (StyledDocument) textPane.getDocument();
        int len = doc.getLength();
        try {
            final int value = scrollbar.getValue();
            boolean atBottom = isAtBottom(value);
            doc.setLogicalStyle(len, style);
            doc.insertString(len, text, null);

            if (atBottom) {
                textPane.setCaretPosition(doc.getLength());
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        scrollbar.setValue(value);
                    }
                });
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isAtBottom(int value) {
        int maximum = scrollbar.getMaximum();
        int amount = scrollbar.getVisibleAmount();
        return value >= maximum - amount - 16;
    }

    public void setCaretColor(Color color) {
        textPane.setCaretColor(color);
    }

    public String getText() {
        return textPane.getText();
    }

    public void setText(String s) {
        textPane.setText(s);
    }

    public void appendLine(String s) {
        appendLine(s, null);
    }

    public void requestFocus() {
        textPane.requestFocus();
    }

    @Override
    public void addKeyListener(KeyListener l) {
        textPane.addKeyListener(l);
    }
}
