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

	private StringRef current;
    private KahluaThread thread;

    public AutoComplete(JFrame window, final JTextComponent component, Platform platform, KahluaTable env) {
		super(new BorderLayout());
		this.window = window;
		this.component = component;
        this.env = env;
        thread = new KahluaThread(platform, env);
        menu = new Menu(this, this.window);
        tooltip = new Tooltip(this.window);
        current = new StringRef(component);
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
                    menu.setVisible(false);
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
                    menu.setVisible(false);
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
                menu.setVisible(false);
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
        component.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                tooltip.setVisible(false);
                if (menu.isVisible()) {
                    int beginIndex = e.getOffset();
                    int endIndex = beginIndex + e.getLength();
                    String newCharacters = component.getText().substring(beginIndex, endIndex);
                    for (char c : newCharacters.toCharArray()) {
                        if (!validCharacter(c)) {
                            current.clear();
                            menu.setMatches(Collections.<CompletionItem>emptyList());
                            menu.setVisible(false);
                            return;
                        }
                    }
                    current.increaseLength(e.getLength());
                    updateMenu();
                }
            }

            public void removeUpdate(DocumentEvent e) {
                tooltip.setVisible(false);
                if (menu.isVisible()) {
                    current.decreaseLength(e.getLength());
                    updateMenu();
                }
            }

            public void changedUpdate(DocumentEvent e) {
                tooltip.setVisible(false);
            }
        });
	}

    private Collection<CompletionItem> getCompletions(String scan) {
        Object cur = env;
        List<String> parts = stringSplit(scan, ".:");
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

    private List stringSplit(String s, String separators) {
        List<String> ret = new ArrayList<String>();
        int pos = 0;
        while (true) {
            int nextPos = findNext(s, separators, pos);
            if (nextPos == -1) {
                ret.add(s.substring(pos));
                return ret;
            } else {
                ret.add(s.substring(pos, nextPos));
                pos = nextPos + 1;
            }
        }
    }

    private int findNext(String s, String chars, int pos) {
        int nextPos = -1;
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            int tmp = s.indexOf(c, pos);
            if (tmp != -1) {
                if (nextPos == -1) {
                    nextPos = tmp;
                } else {
                    nextPos = Math.min(nextPos, tmp);
                }
            }
        }
        return nextPos;
    }

    private void showDefinition() {
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
            setCurrent();
            int index = current.getStart();
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
        setCurrent();
		int index = current.getStart();
		try {
            boolean hasSelection = menu.isVisible() && menu.hasSelection();
            if (hasSelection) {
                menu.onSelected();
            } else {
                Rectangle rect = component.getUI().modelToView(component, index);
                menu.display(new Point(rect.x, rect.y + rect.height), component);
                updateMenu();
                component.requestFocus();
            }

		} catch (BadLocationException e) {
		}
	}

	private void updateMenu() {
		Collection<CompletionItem> completions = getCompletions(current.toString());
		menu.setMatches(completions);
	}

    private String findDefinition() {
        String source = component.getText();
		int position = component.getCaretPosition();
		int index = position;
		while (index > 0) {
            char current = source.charAt(index - 1);
            if (validCharacter(current)) {
                index--;
            } else {
                break;
            }
		}
        while (position < component.getText().length()) {
            char current = source.charAt(position);
            if (validCharacter(current)) {
                position++;
            } else {
                break;
            }
        }
        try {
            return component.getText(index, position - index);
        } catch (BadLocationException e) {
            return "";
        }
    }

	private void setCurrent() {
        String source = component.getText();
		int position = component.getCaretPosition();
		int index = position;
		while (index > 0) {
            char current = source.charAt(index - 1);
            if (validCharacter(current)) {
                index--;
            } else {
                break;
            }
		}
        current.set(index, position - index);
	}

    private boolean validCharacter(char current) {
        return Character.isJavaIdentifierPart(current) ||
                current == '.' || current == ':';
    }

    public String getCompletedCurrent() {
        setCurrent();
        String current = this.current.toString();
        int end = Math.max(current.lastIndexOf('.'), current.lastIndexOf(':')) + 1;
        return current.substring(0, end);
    }

    public String getCurrentlySelectedWord() {
        CompletionItem completionItem = null;
        if (menu.isVisible()) {
            completionItem = menu.getCurrentItem();
        }
        if (completionItem == null) {
            return findDefinition();
        }
        return getCompletedCurrent() + completionItem.getText();
    }

    void setCurrent(String selectedItem) {
		menu.setVisible(false);
		if (selectedItem != null) {
            String current = this.current.toString();
            int from = Math.max(current.lastIndexOf('.'), current.lastIndexOf(':')) + 1;
            int len = current.length() - from;
            if (selectedItem.length() > len) {
				try {
                    String newLetters = selectedItem.substring(len);
                    component.getDocument().remove(this.current.getEnd() - len, len);
                    component.getDocument().insertString(this.current.getEnd() - len, selectedItem, null);
				} catch (BadLocationException e) {
				}
				this.current.increaseLength(selectedItem.length() - len);
			}
		}
		component.requestFocus();
		component.setCaretPosition(current.getEnd());
        current.clear();
	}
}
