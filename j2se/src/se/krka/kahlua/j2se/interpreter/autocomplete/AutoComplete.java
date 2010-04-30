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

import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.*;

import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class AutoComplete extends JPanel {
    private final Menu menu;
    private final Tooltip tooltip;
	private final JTextComponent component;
	private final Window window;
    private final KahluaTable env;

    private final WordFinder wordFinder;

    private final KahluaThread thread;
    private final LuaAutoCompleteSet characterSet;

    public AutoComplete(JFrame window, final JTextComponent component, Platform platform, KahluaTable env) {
		super(new BorderLayout());
		this.window = window;
		this.component = component;
        this.env = env;
        thread = new KahluaThread(platform, env);
        characterSet = new LuaAutoCompleteSet();
        wordFinder = new WordFinder(component.getDocument(), characterSet);
        menu = new Menu(this, this.window);
        tooltip = new Tooltip(this.window);
        add(this.component, BorderLayout.CENTER);

        component.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                component.requestFocus();
            }
        });
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), "open");
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), "definition");
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc");
        component.getActionMap().put("open", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                openMenu();
            }
        });
        component.getActionMap().put("esc", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (tooltip.isVisible()) {
                    tooltip.setVisible(false);
                } else {
                    hideAll();
                }
            }
        });
        component.getActionMap().put("definition", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showDefinition();
            }
        });
        component.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (menu.isVisible()) {
                    hideAll();
                }
            }
        });
        component.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.isConsumed()) {
                    return;
                }

                if (menu.isVisible()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ENTER:
                            menu.onSelected();
                            tooltip.setVisible(false);
                            e.consume();
                            break;
                        case KeyEvent.VK_DOWN:
                            menu.moveDown();
                            tooltip.setVisible(false);
                            e.consume();
                            break;
                        case KeyEvent.VK_UP:
                            menu.moveUp();
                            tooltip.setVisible(false);
                            e.consume();
                            break;
                        case KeyEvent.VK_PAGE_DOWN:
                            menu.movePageDown();
                            tooltip.setVisible(false);
                            e.consume();
                            break;
                        case KeyEvent.VK_PAGE_UP:
                            menu.movePageUp();
                            tooltip.setVisible(false);
                            e.consume();
                            break;
                        case KeyEvent.VK_HOME:
                            menu.moveStart();
                            tooltip.setVisible(false);
                            e.consume();
                            break;
                        case KeyEvent.VK_END:
                            menu.moveEnd();
                            tooltip.setVisible(false);
                            e.consume();
                            break;
                    }
                }
            }
        });
        window.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                hideAll();
            }

            public void componentMoved(ComponentEvent e) {
                if (menu.isVisible()) {
                    menu.moveWindow(component);
                }
                if (tooltip.isVisible()) {
                    tooltip.moveWindow(component);
                }
            }
        });
        component.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (menu.isVisible()) {
                    if (!menu.isWorkingAt(e.getDot())) {
                        hideAll();
                    }
                }

            }
        });
        component.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if (menu.isVisible()) {
                    updateMenu();
                }
            }

            public void removeUpdate(DocumentEvent e) {
                if (menu.isVisible()) {
                    updateMenu();
                }
            }

            public void changedUpdate(DocumentEvent e) {
                if (menu.isVisible()) {
                    updateMenu();
                }
            }
        });
	}

    private void hideAll() {
        tooltip.setVisible(false);
        menu.close();
    }

    private Collection<CompletionItem> getCompletions(String scan) {
        Object cur = env;
        List<String> parts = characterSet.split(scan);
        for (int i = 0; i < parts.size() - 1; i++) {
            if (cur == null) {
                return Collections.emptyList();
            }
            cur = getTableEntry(cur, parts.get(i));
        }
        String lastPart = parts.isEmpty() ? "" : parts.get(parts.size() - 1);

        Set<CompletionItem> returnSet = new TreeSet<CompletionItem>();

        populateSet(returnSet, cur, lastPart);

        return returnSet;
	}

    private void populateSet(Set<CompletionItem> returnSet, Object obj, String needle) {
        for (int i = 0; i < 20; i++) {
            if (obj == null) {
                return;
            }
            if (obj instanceof KahluaTable) {
                KahluaTable table = (KahluaTable) obj;
                KahluaTableIterator iter = table.iterator();
                while (iter.advance()) {
                    Object o = iter.getKey();
                    if (o != null && o instanceof String) {
                        String key = (String) o;
                        Object value = iter.getValue();
                        String extraInfo = getExtraInfo(value);
                        CompletionItem item = new CompletionItem(key, extraInfo, needle);
                        if (item.getScore() > 0) {
                            returnSet.add(item);
                        }
                    }
                }
            }
            obj = getMetaIndex(obj);
        }
    }

    private String getExtraInfo(Object value) {
        String type = BaseLib.type(value);
        if (type.equals("nil") ||
            type.equals("table") ||
            type.equals("function")) {
            return "[" + type + "]";
        }
        return BaseLib.tostring(value, thread);
    }

    private Object getTableEntry(Object obj, String key) {
        return thread.tableGet(obj, key);
    }

    private Object getMetaIndex(Object obj) {
        Object metaTable = thread.getmetatable(obj, true);
        if (metaTable == null) {
            return null;
        }
        return thread.tableGet(metaTable, "__index");
    }

    void showDefinition() {
        String def = getCurrentlySelectedWord().replace(':', '.');
        try {
            LuaClosure closure = LuaCompiler.loadstring("return definition(" + def + ")", "tmp", env);
            Object[] objects = thread.pcall(closure);
            if (objects[0] == Boolean.TRUE && objects.length >= 2 && objects[1] != null && objects[1] instanceof String) {
                showDefinition((String) objects[1]);
            }
        } catch (KahluaException e) {
            return;
        } catch (IOException e) {
            return;
        }
    }

    private void showDefinition(String def) {
        try {
            Word word = wordFinder.findBothWays(component.getCaretPosition());
            int index = word.getStart();
            Rectangle rect = component.getUI().modelToView(component, index);
            if (menu.isVisible()) {
                rect.grow(0, menu.getHeight());
            }
            tooltip.setDefinition(def);
            tooltip.display(new Point(rect.x, rect.y + rect.height), component);
            component.requestFocus();
        } catch (BadLocationException e) {
        }
    }


    private void openMenu() {
        tooltip.setVisible(false);
        if (menu.isVisible() && menu.hasSelection()) {
            menu.onSelected();
        } else {
            updateMenu();
        }
	}

    private void updateMenu() {
        try {
            Word word = wordFinder.findBackwards(component.getCaretPosition());
            int index = word.getStart();
            Rectangle rect = component.getUI().modelToView(component, index);
            menu.display(new Point(rect.x, rect.y + rect.height), component);
            Collection<CompletionItem> completions = getCompletions(word.toString());
            menu.setMatches(word, completions);
            component.requestFocus();
        } catch (BadLocationException e) {
        }
    }

    public String getCompletedCurrent() {
        Word word = wordFinder.findBothWays(component.getCaretPosition());
        word = wordFinder.withoutLastPart(word);
        return word.toString();
    }

    public String getCurrentlySelectedWord() {
        CompletionItem completionItem = null;
        if (menu.isVisible()) {
            completionItem = menu.getCurrentItem();
        }
        if (completionItem == null) {
            return wordFinder.findBothWays(component.getCaretPosition()).toString();
        }
        return getCompletedCurrent() + completionItem.getText();
    }

    void finishAutocomplete(final Word word, final String selectedItem) {
        hideAll();
        if (selectedItem != null) {
            final Word fullWord = wordFinder.findBothWays(component.getCaretPosition());
            final Word firstPart = wordFinder.withoutLastPart(word);
            final String newText = firstPart.toString() + selectedItem;
            final int startPos = fullWord.getStart();
            try {
                component.getDocument().remove(startPos, fullWord.length());
                component.getDocument().insertString(firstPart.getStart(), newText, null);
            } catch (BadLocationException e) {
                return;
            }
            component.requestFocus();
            component.setCaretPosition(firstPart.getStart() + newText.length());
		}
	}
}
