package se.krka.kahlua.j2se.interpreter;

import se.krka.kahlua.converter.KahluaEnumConverter;
import se.krka.kahlua.converter.KahluaTableConverter;
import se.krka.kahlua.converter.LuaConverterManager;
import se.krka.kahlua.converter.LuaNumberConverter;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.vm.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;

public class InteractiveShell {
    private final JMenuItem newInterpreter;
    private final JMenu windowMenu;
    private final AtomicInteger counter = new AtomicInteger();

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
        final JFrame frame = new JFrame("Kahlua interpreter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JDesktopPane mdi = new JDesktopPane();
        frame.getContentPane().add(mdi);

        JMenuBar menuBar = new JMenuBar();

        JMenu mainMenu = new JMenu("Main");
        menuBar.add(mainMenu);

        windowMenu = new JMenu("Windows");
        menuBar.add(windowMenu);

        newInterpreter = new JMenuItem("New interpreter");
        newInterpreter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = "Window " + counter.incrementAndGet();
                final JInternalFrame intframe = new JInternalFrame(name,true,true,true,true);
                intframe.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
                JPanel interpreter1 = new Interpreter(platform, env, frame);

                JMenuItem item = new JMenuItem(name);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        intframe.moveToFront();
                        intframe.setVisible(true);
                    }
                });
                windowMenu.add(item);


                intframe.setSize(600, 400);
                intframe.getContentPane().add(interpreter1);
                intframe.setVisible(true);
                intframe.moveToFront();
                mdi.add(intframe);

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
    }
}
