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

import se.krka.kahlua.converter.LuaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.Platform;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Interpreter extends JPanel {
    private final KahluaThread state;
    private final Terminal terminal;
    private final JLabel outputTitle = new JLabel("Output:");
    private final JLabel inputTitle = new JLabel("Input:");


    private final Style errorStyle;
    private final History history = new History();
    private final ExecutorService executors = Executors.newSingleThreadExecutor();
    private Future<?> future;

    LuaConverterManager manager = new LuaConverterManager();
    LuaCaller caller = new LuaCaller(manager);

    public Interpreter(Platform platform, KahluaTable env) {
        super(new BorderLayout());

        terminal = new Terminal(false, Color.WHITE);
        terminal.setPreferredSize(new Dimension(800, 400));

        Color inputColor = Color.GREEN.brighter().brighter().brighter();
        final Style inputStyle = terminal.createStyle("input", inputColor);
        errorStyle = terminal.createStyle("error", Color.RED.brighter().brighter());

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(outputTitle, BorderLayout.NORTH);
        outputPanel.add(terminal, BorderLayout.CENTER);
        this.add(outputPanel, BorderLayout.CENTER);

        final Terminal input = new Terminal(true, Color.WHITE);
        input.setPreferredSize(new Dimension(800, 100));
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputTitle, BorderLayout.NORTH);
        inputPanel.add(input, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.SOUTH);

        state = new KahluaThread(terminal.getPrintStream(), platform, env);

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
                if (isControl(keyEvent)) {
                    if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (isDone()) {
                            String text = input.getText();
                            history.add(text);
                            terminal.appendLine(text, inputStyle);
                            input.setText("");
                            execute(text);
                       }
                        keyEvent.consume();
                    }
                    if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
                        history.moveBack(input);
                        keyEvent.consume();
                    }
                    if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
                        history.moveForward(input);
                        keyEvent.consume();
                    }
                }
            }
        });


        terminal.appendLine("Welcome to the Kahlua interpreter");
        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
            }

            @Override
            public void componentShown(ComponentEvent componentEvent) {
                input.requestFocus();
            }

            @Override
            public void componentHidden(ComponentEvent componentEvent) {
            }
        });
    }

    private boolean isControl(KeyEvent keyEvent) {
        return (keyEvent.getModifiers() & KeyEvent.CTRL_MASK) != 0;
    }

    private void execute(final String text) {
        future = executors.submit(new Runnable() {
            @Override
            public void run() {
                outputTitle.setText("Output: [running...]");
                try {
                    LuaClosure luaClosure = LuaCompiler.loadstring(text, "<interpreter>", state.getEnvironment());
                    LuaReturn result = caller.protectedCall(state, luaClosure);
                    if (result.isSuccess()) {
                        for (Object o : result) {
                            terminal.appendLine(BaseLib.tostring(o, state));
                        }
                    } else {
                        terminal.appendLine(result.getErrorString(), errorStyle);
                        terminal.appendLine(result.getLuaStackTrace(), errorStyle);
                        result.getJavaException().printStackTrace(System.err);
                    }
                } catch (IOException e) {
                    e.printStackTrace(terminal.getPrintStream());
                } catch (KahluaException e) {
                    terminal.appendLine(e.getMessage(), errorStyle);
                }
                outputTitle.setText("Output:");
            }
        });
    }

    public boolean isDone() {
        return future == null || future.isDone();
    }
}
