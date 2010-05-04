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

import se.krka.kahlua.j2se.interpreter.jsyntax.JSyntaxUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class OutputTerminal extends JPanel implements FocusListener {
    private final JScrollPane scrollpane;
    private boolean scrollDown;
    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(1, 5, 1, 5);
    private final Font font;

    private static enum Type {
        NONE, LUA, OUTPUT, ERROR, INFO
    }
    private Type currentType = Type.NONE;
    private JTextComponent current;

    private final JComponent view;
    private final Color background;
    private final Color errorColor = Color.RED.brighter().brighter();
    private final Color infoColor = Color.GREEN.brighter().brighter().brighter();
    private final Color outputColor = Color.WHITE;

    private final PrintStream printStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            appendOutput(String.valueOf((char) b));
        }
    });

    public OutputTerminal(Color background, Font font) {
        super(new BorderLayout());
        this.background = background;
        this.font = font;
        view = new JPanel();
        view.setBorder(BorderFactory.createEmptyBorder());
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));

        JComponent filler = new JPanel(new BorderLayout());
        filler.add(view, BorderLayout.NORTH);
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(background);
        filler.add(panel, BorderLayout.CENTER);

        scrollpane = new JScrollPane(
                filler,
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
        createLuaPane();
        append(text, current);
    }

    public synchronized void appendOutput(String text) {
        if (currentType != Type.OUTPUT) {
            createOutputPane();
        }
        append(text, current);
    }

    public synchronized void appendError(String text) {
        if (currentType != Type.ERROR) {
            createErrorPane();
        }
        append(text, current);
    }

    public synchronized void appendInfo(String text) {
        if (currentType != Type.INFO) {
            createInfoPane();
        }
        append(text, current);
    }

    private void createOutputPane() {
        createPane(outputColor);
        currentType = Type.OUTPUT;
    }

    private void createInfoPane() {
        createPane(infoColor);
        currentType = Type.INFO;
    }

    private void createErrorPane() {
        createPane(errorColor);
        currentType = Type.ERROR;
    }

    private void createPane(Color color) {
        JTextArea pane = new JTextArea();
        pane.setFont(font);
        setup(pane);
        pane.setBackground(background);
        pane.setForeground(color);
        view.add(pane);
        current = pane;
    }

    private void setup(JTextComponent pane) {
        pane.setBorder(EMPTY_BORDER);
        pane.setEditable(false);
        pane.setFocusable(true);
        pane.addFocusListener(this);
    }

    private synchronized void append(final String text, final JTextComponent current) {
        JScrollBar vert = scrollpane.getVerticalScrollBar();
        boolean isAtBottom = vert.getValue() + vert.getVisibleAmount() >= vert.getMaximum() - 32;
        scrollDown = !vert.getValueIsAdjusting() && isAtBottom;

        try {
            Document document = current.getDocument();
            document.insertString(document.getLength(), text, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void createLuaPane() {
        JEditorPane pane = new JEditorPane();
        pane.setBackground(background);

        JSyntaxUtil.installSyntax(pane, false);
        view.add(pane);

        current = pane;
        currentType = Type.LUA;
        setup(current);
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
    
}
