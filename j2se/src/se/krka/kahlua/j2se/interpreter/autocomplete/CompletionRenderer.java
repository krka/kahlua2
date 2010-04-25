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

package se.krka.kahlua.j2se.interpreter.autocomplete;

import javax.swing.*;
import java.awt.*;

public class CompletionRenderer extends JPanel implements ListCellRenderer {

    private final JLabel left = new JLabel();
    private final JLabel right = new JLabel();
    private final JPanel spacer = new JPanel();

    public CompletionRenderer() {
        setLayout(new BorderLayout());
        spacer.setMinimumSize(new Dimension(40, 1));

        left.setOpaque(true);
        right.setOpaque(true);
        spacer.setOpaque(true);

        add(left, BorderLayout.WEST);
        add(spacer, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        CompletionItem item = (CompletionItem) value;
        left.setText(item.getText());
        right.setText(item.getValue());

        Color back;
        Color front;
        if (isSelected) {
            back = list.getSelectionBackground();
            front = list.getSelectionForeground();
        } else {
            back = list.getBackground();
            front = list.getForeground();
        }

        setColor(left, back, front);
        setColor(right, back, front);
        setColor(spacer, back, front);

        setEnabled(list.isEnabled());
        setFont(list.getFont());        

        return this;
    }

    private void setColor(JComponent component, Color back, Color front) {
        component.setBackground(back);
        component.setForeground(front);
    }
}
