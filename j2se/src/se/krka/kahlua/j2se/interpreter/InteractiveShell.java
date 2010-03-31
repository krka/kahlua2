package se.krka.kahlua.j2se.interpreter;

import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaState;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class InteractiveShell {
    public static void main(final String[] args) {
        JFrame frame = new JFrame("Kahlua interpreter");

        JPanel interpreter1 = createInterpreter();
        JPanel interpreter2 = createInterpreter();

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("First", interpreter1);
        tabs.add("Second", interpreter2);
        frame.getContentPane().add(tabs);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    private static JPanel createInterpreter() {
        JPanel main = new JPanel(new BorderLayout());

        final Terminal terminal = new Terminal(false, Color.WHITE);
        terminal.setPreferredSize(new Dimension(800, 400));

        Color inputColor = Color.GREEN.brighter().brighter().brighter();
        final Style inputStyle = terminal.createStyle("input", inputColor);
        final Style errorStyle = terminal.createStyle("error", Color.RED.brighter().brighter());

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(new JLabel("Output"), BorderLayout.NORTH);
        outputPanel.add(terminal, BorderLayout.CENTER);
        main.add(outputPanel, BorderLayout.CENTER);

        final Terminal input = new Terminal(true, Color.WHITE);
        input.setPreferredSize(new Dimension(800, 100));
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Input"), BorderLayout.NORTH);
        inputPanel.add(input, BorderLayout.CENTER);
        main.add(inputPanel, BorderLayout.SOUTH);

        final LuaState state = new LuaState(
                terminal.getPrintStream(),
                new J2SEPlatform());
        state.getEnvironment().rawset("sleep", new JavaFunction() {
            @Override
            public int call(LuaCallFrame callFrame, int nArguments) {
                double seconds = KahluaUtil.getDoubleArg(callFrame, 1, "sleep");
                try {
                    Thread.sleep((long) (seconds * 1000));
                } catch (InterruptedException e) {
                }
                return 0;
            }
        });

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
						String text = input.getText();
						terminal.appendLine(text, inputStyle);
						input.setText("");
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
				}
			}
        });


        terminal.appendLine("Welcome to the Kahlua interpreter");
        input.requestFocus();
        return main;
    }
}
