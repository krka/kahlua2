package se.krka.kahlua.j2se.interpreter;

import se.krka.kahlua.converter.KahluaEnumConverter;
import se.krka.kahlua.converter.KahluaTableConverter;
import se.krka.kahlua.converter.LuaConverterManager;
import se.krka.kahlua.converter.LuaNumberConverter;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.Platform;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InteractiveShell {
    private final JFrame frame;
    private final JMenuItem newInterpreter;
    private final JMenu windowMenu;
    private final AtomicInteger counter = new AtomicInteger();
    private final List<JInternalFrame> windows;
    private final List<Interpreter> interpreters;

    public static void main(final String[] args) {
        final Platform platform = new J2SEPlatform();
        final KahluaTable env = platform.newEnvironment();

        LuaConverterManager manager = new LuaConverterManager();
        LuaNumberConverter.install(manager);
        KahluaEnumConverter.install(manager);
        new KahluaTableConverter(platform).install(manager);
        LuaJavaClassExposer exposer = new LuaJavaClassExposer(manager, platform, env);

        exposer.exposeGlobalFunctions(exposer);
        KahluaTable staticBase = platform.newTable();
        env.rawset("Java", staticBase);
        exposer.exposeLikeJavaRecursively(Object.class, staticBase);

        exposer.exposeGlobalFunctions(new Sleeper());

        new InteractiveShell(platform, env);
    }

    public InteractiveShell(final Platform platform, final KahluaTable env) {
        frame = new JFrame("Kahlua interpreters");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JDesktopPane mdi = new JDesktopPane();
        frame.getContentPane().add(mdi);

        JMenuBar menuBar = new JMenuBar();

        JMenu mainMenu = new JMenu("Main");
        menuBar.add(mainMenu);

        windowMenu = new JMenu("Windows");
        menuBar.add(windowMenu);

        windows = new ArrayList<JInternalFrame>();
        interpreters = new ArrayList<Interpreter>();

        newInterpreter = new JMenuItem("New interpreter");
        newInterpreter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = "Window " + counter.incrementAndGet();
                final JInternalFrame frame = new JInternalFrame(name,true,true,true,true);
                frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
                Interpreter interpreter = new Interpreter(platform, env, InteractiveShell.this.frame);

                windows.add(frame);
                interpreters.add(interpreter);

                JMenuItem item = new JMenuItem(name);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frame.moveToFront();
                        frame.setVisible(true);
                    }
                });
                windowMenu.add(item);


                frame.setSize(600, 400);
                frame.getContentPane().add(interpreter);
                frame.setVisible(true);
                frame.moveToFront();
                mdi.add(frame);

            }
        });
        mainMenu.add(newInterpreter);
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        mainMenu.add(exit);


        frame.setJMenuBar(menuBar);

        frame.setSize(800, 600);
        frame.setVisible(true);

        newInterpreter.doClick();
        try {
            getWindow(0).setMaximum(true);
        } catch (PropertyVetoException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public JInternalFrame getWindow(int index) {
        return windows.get(index);
    }

    public Interpreter getInterpreter(int index) {
        return interpreters.get(index);
    }

}
