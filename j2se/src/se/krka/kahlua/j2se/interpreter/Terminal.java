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

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyListener;

public class Terminal extends JPanel {
    private final JEditorPane textPane;
    private final JScrollPane scrollPane;

    public Terminal(boolean editable, Color background, Color foreground) {
        super(new BorderLayout());
        textPane = new JEditorPane();
        textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setBackground(background);
        setForeground(foreground);
        textPane.setCaretColor(foreground);

        textPane.setEditable(editable);
        textPane.setCaretPosition(0);

        JComponent scrollChild = textPane;

        scrollPane = new JScrollPane(
                scrollChild,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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

    public String getText() {
        return textPane.getText();
    }

    public void setText(String s) {
        textPane.setText(s);
    }

    public void requestFocus() {
        textPane.requestFocus();
    }

    @Override
    public void addKeyListener(KeyListener l) {
        textPane.addKeyListener(l);
    }

    public JEditorPane getTextPane() {
        return textPane;
    }
}
