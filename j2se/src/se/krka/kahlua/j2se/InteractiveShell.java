package se.krka.kahlua.j2se;

import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaState;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class InteractiveShell {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Kahlua interpreter");
        JPanel main = new JPanel(new BorderLayout());

        final JTextPane output = new JTextPane();
		output.setBackground(Color.BLACK);
		Color inputColor = Color.GREEN.brighter().brighter().brighter();
		createStyle(output, "input", inputColor);
		output.setPreferredSize(new Dimension(1, 400));
		createStyle(output, "output", Color.WHITE);
		createStyle(output, "error", Color.RED.brighter().brighter());

		JScrollPane outputScrollPane = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel outputPanel = new JPanel(new BorderLayout());
		outputPanel.add(new JLabel("Output"), BorderLayout.NORTH);
		outputPanel.add(outputScrollPane, BorderLayout.CENTER);
		main.add(outputPanel, BorderLayout.CENTER);

        final JTextArea input = new JTextArea();
		input.setBackground(Color.BLACK);
		input.setForeground(inputColor);
		input.setCaretColor(Color.WHITE);
        JPanel inputPanel = new JPanel(new BorderLayout());
		JScrollPane inputScrollPane = new JScrollPane(input, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inputPanel.add(new JLabel("Input"), BorderLayout.NORTH);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        main.add(inputPanel, BorderLayout.SOUTH);


		final OutputStream outputStream = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				append(output, "output", String.valueOf((char) b));
			}


		};
		final PrintStream printStream = new PrintStream(outputStream);
		final LuaState state = new LuaState(
				printStream,
				new J2SEPlatform());
        output.setEditable(false);

        input.setColumns(80);
        input.setRows(3);
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
					if ((keyEvent.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
						input.append("\n");
					} else {
						String text = input.getText();
						append(output, "input", "> " + text);
						input.setText("");
						try {
							LuaClosure luaClosure = LuaCompiler.loadstring(text, "@interpreter", state.getEnvironment());
							Object[] res = state.pcall(luaClosure);
							if (res[0] == Boolean.TRUE) {
								for (int i = 1; i < res.length; i++) {
									if (res[i] == null) {
										append(output, "output", "nil\n");
									} else {
										append(output, "output", res[i].toString() + "\n");
									}
								}
							} else {
								for (int i = 1; i < res.length; i++) {
									if (res[i] != null) {
										append(output, "error", res[i].toString() + "\n");
									}
								}
							}
						} catch (IOException e) {
							e.printStackTrace(printStream);
						} catch (KahluaException e) {
							append(output, "error", e.getMessage() + "\n");
						}
					}
				}
			}
        });

        frame.getContentPane().add(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

		append(output, "output", "Welcome to the Kahlua interpreter\n");
		input.requestFocus();
    }

	private static void createStyle(JTextPane output, String s, Color color) {
		Style style = output.addStyle(s, null);
		StyleConstants.setForeground(style, color);
	}

	private static void append(JTextPane output, String style, String s) {
		StyledDocument doc = (StyledDocument) output.getDocument();
		int len = doc.getLength();
		try {
			doc.setLogicalStyle(len, doc.getStyle(style));
			doc.insertString(len, s, null);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}
}
