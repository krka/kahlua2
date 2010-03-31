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

import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.Platform;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Interpreter extends JPanel {
    private final LuaState state;
    private final Terminal terminal;
    private final Style errorStyle;

    private final ExecutorService executors = Executors.newSingleThreadExecutor();
    private Future<?> future;

    public Interpreter(Platform platform, KahluaTable env) {
        super(new BorderLayout());

        terminal = new Terminal(false, Color.WHITE);
        terminal.setPreferredSize(new Dimension(800, 400));

        Color inputColor = Color.GREEN.brighter().brighter().brighter();
        final Style inputStyle = terminal.createStyle("input", inputColor);
        errorStyle = terminal.createStyle("error", Color.RED.brighter().brighter());

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(new JLabel("Output"), BorderLayout.NORTH);
        outputPanel.add(terminal, BorderLayout.CENTER);
        this.add(outputPanel, BorderLayout.CENTER);

        final Terminal input = new Terminal(true, Color.WHITE);
        input.setPreferredSize(new Dimension(800, 100));
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Input"), BorderLayout.NORTH);
        inputPanel.add(input, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.SOUTH);

        state = new LuaState(terminal.getPrintStream(), platform, env);

        input.setPreferredSize(new Dimension(800, 100));
        input.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    if ((keyEvent.getModifiers() & KeyEvent.CTRL_MASK) == 0) {
                    } else {
                        if (isDone()) {
                            String text = input.getText();
                            terminal.appendLine(text, inputStyle);
                            input.setText("");
                            execute(text);
                        }
                    }
                }
            }
        });


        terminal.appendLine("Welcome to the Kahlua interpreter");
        input.requestFocus();
    }

    private void execute(final String text) {
        future = executors.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    LuaClosure luaClosure = LuaCompiler.loadstring(text, "<interpreter>", state.getEnvironment());
                    Object[] res = state.pcall(luaClosure);
                    if (res[0] == Boolean.TRUE) {
                        for (int i = 1; i < res.length; i++) {
                            if (res[i] == null) {
                                terminal.appendLine("nil");
                            } else {
                                terminal.appendLine(res[i].toString());
                            }
                        }
                    } else {
                        for (int i = 1; i < res.length; i++) {
                            if (res[i] != null) {
                                terminal.appendLine(res[i].toString(), errorStyle);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace(terminal.getPrintStream());
                } catch (KahluaException e) {
                    terminal.appendLine(e.getMessage(), errorStyle);
                }
            }
        });
    }

    public boolean isDone() {
        return future == null || future.isDone();
    }
}