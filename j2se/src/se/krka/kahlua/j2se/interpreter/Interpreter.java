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
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.interpreter.autocomplete.AutoComplete;
import se.krka.kahlua.j2se.interpreter.jsyntax.JSyntaxUtil;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.*;

import javax.swing.*;
import java.awt.*;
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
    private final OutputTerminal output;
    private final JLabel outputTitle = new JLabel("Output:");
    private final JLabel inputTitle = new JLabel("Input:");

    private final History history = new History();
    private final ExecutorService executors = Executors.newSingleThreadExecutor(new DaemonizedThreadFactory(Executors.defaultThreadFactory()));
    private Future<?> future;

    final LuaConverterManager manager = new LuaConverterManager();
    final LuaCaller caller = new LuaCaller(manager);
    final LuaJavaClassExposer exposer;

    public Interpreter(Platform platform, KahluaTable env, JFrame owner) {
        super(new BorderLayout());

        JSyntaxUtil.setup();

        exposer = new LuaJavaClassExposer(manager, platform, env);
        exposer.exposeGlobalFunctions(this);

        final Terminal input = new Terminal(true, Color.BLACK, Color.WHITE);

        JSyntaxUtil.installSyntax(input.getTextPane());
        new AutoComplete(owner, input.getTextPane(), platform, env);


        output = new OutputTerminal(Color.BLACK, input.getTextPane().getFont());
        output.setPreferredSize(new Dimension(800, 400));
        output.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() != 0) {
                    input.getTextPane().requestFocus();
                    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(outputTitle, BorderLayout.NORTH);
        outputPanel.add(output, BorderLayout.CENTER);
        this.add(outputPanel, BorderLayout.CENTER);



        input.setPreferredSize(new Dimension(800, 100));
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputTitle, BorderLayout.NORTH);
        inputPanel.add(input, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.SOUTH);

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
                            output.appendLua(text);
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

        state = new KahluaThread(output.getPrintStream(), platform, env);
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
                    LuaClosure luaClosure = smartCompile(text);
                    LuaReturn result = caller.protectedCall(state, luaClosure);
                    if (result.isSuccess()) {
                        for (Object o : result) {
                            output.appendOutput(BaseLib.tostring(o, state)+"\n");
                        }
                    } else {
                        output.appendError(result.getErrorString()+"\n");
                        output.appendError(result.getLuaStackTrace()+"\n");
                        result.getJavaException().printStackTrace(System.err);
                    }
                } catch (IOException e) {
                    e.printStackTrace(output.getPrintStream());
                } catch (RuntimeException e) {
                    output.appendError(e.getMessage()+"\n");
                }
                outputTitle.setText("Output:");
            }
        });
    }

    private LuaClosure smartCompile(String text) throws IOException {
        LuaClosure luaClosure;
        try {
            luaClosure = LuaCompiler.loadstring("return " + text, "<interpreter>", state.getEnvironment());
        } catch (KahluaException e) {
            // Ignore it and try without "return "
            luaClosure = LuaCompiler.loadstring(text, "<interpreter>", state.getEnvironment());
        }
        return luaClosure;
    }

    public boolean isDone() {
        return future == null || future.isDone();
    }

    public KahluaThread getState() {
        return state;
    }

    public OutputTerminal getOutput() {
        return output;
    }
}
