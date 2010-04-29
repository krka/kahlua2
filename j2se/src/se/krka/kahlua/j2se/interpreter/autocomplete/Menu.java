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
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class Menu extends HelperWindow {
    private final JList visualList;
    private final SmartListModel listModel;
    private final AutoComplete autoComplete;
    private CompletionItem currentItem;

    public Menu(AutoComplete autoComplete, Window window) {
        super(window);
        this.autoComplete = autoComplete;
        listModel = new SmartListModel();
        visualList = new JList(listModel) {
            public int getVisibleRowCount() {
                return Math.min(listModel.getSize(), 10);
            }
        };
        visualList.setCellRenderer(new CompletionRenderer());
        visualList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(visualList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setContentPane(scrollPane);
        visualList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onSelected();
                }
            }
        });
        visualList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1) &&
                        (e.getClickCount() >= 2)) {
                    onSelected();
                }
            }
        });
    }

    void onSelected() {
        CompletionItem completionItem = getCurrentItem();
        if (completionItem != null) {
            autoComplete.setCurrent(completionItem.getText());
        }
    }

    public void setMatches(Collection<CompletionItem> matches) {
        listModel.setContent(matches);
        if (matches.isEmpty()) {
            setVisible(false);
            return;
        }
        if (getNumElements() > 0 && visualList.getSelectedIndex() < 0) {
            moveStart();
        }
        pack();
        pack();
    }

    public void moveDown() {
        moveRelative(1);
    }

    public void moveUp() {
        moveRelative(-1);
    }

    public void moveStart() {
        moveTo(0);
    }

    public void moveEnd() {
        moveTo(listModel.getSize() - 1);
    }

    public void movePageUp() {
        moveRelative(-(visualList.getVisibleRowCount() - 1));
    }

    public void movePageDown() {
        moveRelative(visualList.getVisibleRowCount() - 1);
    }

    public void moveRelative(int distance) {
        int size = listModel.getSize();

        if (size == 0) {
            return;
        }

        int currentIndex = visualList.getSelectedIndex();
        int newIndex = (currentIndex + distance) % size;
        if (newIndex < 0) {
            newIndex += size;
        }
        moveTo(newIndex);
    }

    private void moveTo(int index) {
        int size = listModel.getSize();
        if (index < 0 || index >= size) {
            return;
        }
        visualList.setSelectionInterval(index, index);
        visualList.scrollRectToVisible(visualList.getCellBounds(index, index));
    }

    public int getNumElements() {
        return listModel.getSize();
    }

    public CompletionItem getCurrentItem() {
        return (CompletionItem) visualList.getSelectedValue();
    }
}
