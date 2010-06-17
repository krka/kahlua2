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
import java.awt.Component;
import java.awt.Dimension;

public class TestFrame {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JComponent view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.add(new JTextArea("asd"));
        view.add(new JTextArea("asd"));
        view.add(new JTextArea("asd"));
        view.add(new JTextArea("asd"));
        view.add(Box.createGlue());

        JScrollPane scrollpane = new JScrollPane(filler(view));
        frame.getContentPane().add(scrollpane);
        frame.setVisible(true);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.pack();
    }

    private static JPanel filler(JComponent other) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
        panel.add(other, BorderLayout.SOUTH);
        return panel;
    }
}
