package se.krka.kahlua.j2se;

import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaState;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
        output.setFont(new Font("Monospaced", Font.PLAIN, 10));
		output.setBackground(Color.BLACK);
		Color inputColor = Color.GREEN.brighter().brighter().brighter();
		createStyle(output, "input", inputColor);
		output.setPreferredSize(new Dimension(800, 400));
        output.setAutoscrolls(false);
		createStyle(output, "output", Color.WHITE);
		createStyle(output, "error", Color.RED.brighter().brighter());

		final JScrollPane outputScrollPane = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel outputPanel = new JPanel(new BorderLayout());
		outputPanel.add(new JLabel("Output"), BorderLayout.NORTH);
		outputPanel.add(outputScrollPane, BorderLayout.CENTER);
		main.add(outputPanel, BorderLayout.CENTER);

        final JTextPane input = new JTextPane();
		input.setBackground(Color.BLACK);
		input.setForeground(inputColor);
		input.setCaretColor(Color.WHITE);
        input.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JPanel inputPanel = new JPanel(new BorderLayout());
		JScrollPane inputScrollPane = new JScrollPane(input, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inputPanel.add(new JLabel("Input"), BorderLayout.NORTH);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        main.add(inputPanel, BorderLayout.SOUTH);


		final OutputStream outputStream = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				append(output, "output", String.valueOf((char) b), outputScrollPane.getVerticalScrollBar());
			}


		};
		final PrintStream printStream = new PrintStream(outputStream);
		final LuaState state = new LuaState(
				printStream,
				new J2SEPlatform());
        output.setEditable(false);

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
						appendLine(output, "input", text, outputScrollPane.getVerticalScrollBar());
						input.setText("");
						try {
							LuaClosure luaClosure = LuaCompiler.loadstring(text, "<interpreter>", state.getEnvironment());
							Object[] res = state.pcall(luaClosure);
							if (res[0] == Boolean.TRUE) {
								for (int i = 1; i < res.length; i++) {
									if (res[i] == null) {
										appendLine(output, "output", "nil", outputScrollPane.getVerticalScrollBar());
									} else {
										appendLine(output, "output", res[i].toString(), outputScrollPane.getVerticalScrollBar());
									}
								}
							} else {
								for (int i = 1; i < res.length; i++) {
									if (res[i] != null) {
										appendLine(output, "error", res[i].toString(), outputScrollPane.getVerticalScrollBar());
									}
								}
							}
						} catch (IOException e) {
							e.printStackTrace(printStream);
						} catch (KahluaException e) {
							appendLine(output, "error", e.getMessage(), outputScrollPane.getVerticalScrollBar());
						}
					}
				}
			}
        });

        frame.getContentPane().add(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

		appendLine(output, "output", "Welcome to the Kahlua interpreter", outputScrollPane.getVerticalScrollBar());
		input.requestFocus();
    }

	private static void createStyle(JTextPane output, String s, Color color) {
		Style style = output.addStyle(s, null);
		StyleConstants.setForeground(style, color);
	}

    private static void appendLine(JTextPane output, String style, String s, JScrollBar scrollbar) {
        if (!s.endsWith("\n")) {
            s = s + "\n";
        }
        append(output, style, s, scrollbar);
    }

	private static void append(JTextPane output, String style, String s, final JScrollBar scrollbar) {
		StyledDocument doc = (StyledDocument) output.getDocument();
		int len = doc.getLength();
		try {
            final int value = scrollbar.getValue();
            int maximum = scrollbar.getMaximum();
            int amount = scrollbar.getVisibleAmount();
            boolean doScroll = value >= maximum - amount - 8;
			doc.setLogicalStyle(len, doc.getStyle(style));
			doc.insertString(len, s, null);

            if (doScroll) {
                output.setCaretPosition(output.getDocument().getLength());
            } else {
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        scrollbar.setValue(value);
                    }
                });
            }
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}
}
