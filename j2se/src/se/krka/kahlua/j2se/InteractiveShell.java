package se.krka.kahlua.j2se;

import se.krka.kahlua.vm.LuaState;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InteractiveShell {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Kahlua interpreter");
        JPanel main = new JPanel(new BorderLayout());

        final JTextArea output = new JTextArea();
        main.add(new JPanel().add(output), BorderLayout.CENTER);
        final JTextArea input = new JTextArea();
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Input"), BorderLayout.NORTH);
        inputPanel.add(input, BorderLayout.CENTER);
        main.add(inputPanel, BorderLayout.SOUTH);


        //PrintStream outputStream = null;
        //LuaState state = new LuaState(outputStream);
        output.setEditable(false);
        output.setColumns(80);
        output.setRows(25);
        
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
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER &&
                        (keyEvent.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    output.append("> " + input.getText());
                    input.setText("");
                }
            }
        });

        frame.getContentPane().add(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
