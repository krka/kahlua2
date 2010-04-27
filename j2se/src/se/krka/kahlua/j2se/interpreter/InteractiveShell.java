package se.krka.kahlua.j2se.interpreter;

import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.vm.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class InteractiveShell {
    public static void main(final String[] args) {
        JFrame frame = new JFrame("Kahlua interpreter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Platform platform = new J2SEPlatform();
        KahluaTable env = platform.newEnvironment();
        env.rawset("sleep", new JavaFunction() {
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

        InteractiveShell shell = new InteractiveShell(frame, platform, env);
    }

    public InteractiveShell(JFrame frame, Platform platform, KahluaTable env) {
        JPanel interpreter1 = new Interpreter(platform, env, frame);
        JPanel interpreter2 = new Interpreter(platform, env, frame);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFocusable(false);
        tabs.add("First", interpreter1);
        tabs.add("Second", interpreter2);
        frame.getContentPane().add(tabs);
        frame.pack();
        frame.setVisible(true);
    }

}
