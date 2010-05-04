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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Menu extends HelperWindow {
    private final JLabel title;
    private final JList visualList;
    private final SmartListModel listModel;
    private final AutoComplete autoComplete;
    private int startPos;
    private final JScrollPane scrollPane;

    public Menu(final AutoComplete autoComplete, Window window) {
        super(window);
        this.autoComplete = autoComplete;
        listModel = new SmartListModel();
        title = new JLabel("No matches");
        visualList = new JList(listModel) {
            public int getVisibleRowCount() {
                return Math.min(listModel.getSize(), 10);
            }
        };
        visualList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        autoComplete.showDefinition();
                    }
                });
            }
        });
        visualList.setFocusable(false);
        visualList.setCellRenderer(new CompletionRenderer());
        visualList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane = new JScrollPane(visualList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        setContentPane(panel);
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
            autoComplete.finishAutocomplete(startPos, completionItem.getText());
        }
    }

    public void setMatches(Collection<CompletionItem> matches) {
        listModel.setContent(matches);
        if (matches.isEmpty()) {
            title.setVisible(true);
            scrollPane.setVisible(false);
            pack();
            pack();
            return;
        } else {
            title.setVisible(false);
            scrollPane.setVisible(true);
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

    public boolean hasSelection() {
        return visualList.getSelectedIndex() >= 0;
    }

    public boolean isWorkingAt(Word word, int dot) {
        if (!isVisible()) {
            return false;
        }
        if (word.getStart() != startPos) {
            return false;
        }
        return dot >= word.getStart() && dot <= word.getEnd();
    }

    public void close() {
        setVisible(false);
    }

    public void setStartPos(int startpos) {
        this.startPos = startpos;
    }
}
