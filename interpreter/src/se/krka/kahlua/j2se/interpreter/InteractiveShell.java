package se.krka.kahlua.j2se.interpreter;

import se.krka.kahlua.converter.*;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.j2se.Kahlua;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.Platform;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InteractiveShell {
    private final JMenuItem newInterpreter;
    private final JMenu windowMenu;
    private final AtomicInteger counter = new AtomicInteger();
    private final ArrayList<InternalInterpreterFrame> interpreters;
    private final JMenuItem exit = new JMenuItem("Exit");
	private final JFrame frame;

	public static void main(final String[] args) {
		Kahlua kahlua = new Kahlua();
		KahluaNumberConverter.install(kahlua.getConverterManager());
		KahluaEnumConverter.install(kahlua.getConverterManager());
		new KahluaTableConverter(kahlua.getPlatform()).install(kahlua.getConverterManager());

		new InteractiveShell(kahlua);
	}

	public InteractiveShell(final Kahlua kahlua) {
		this(kahlua, createFrame());
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
	}

	private static JFrame createFrame() {
		final JFrame frame = new JFrame("Interactive shell");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

	public InteractiveShell(final Kahlua kahlua, final JFrame frame) {
		this.frame = frame;

		KahluaTable staticBase = kahlua.getPlatform().newTable();
		kahlua.getEnvironment().rawset("Java", staticBase);

        LuaJavaClassExposer exposer = new LuaJavaClassExposer(kahlua.getConverterManager(), kahlua.getPlatform(), kahlua.getEnvironment(), staticBase);

        exposer.exposeGlobalFunctions(exposer);
        exposer.exposeLikeJavaRecursively(Object.class, staticBase);

        exposer.exposeGlobalFunctions(new Sleeper());

        final JDesktopPane mdi = new JDesktopPane();
        frame.getContentPane().add(mdi);

        JMenuBar menuBar = new JMenuBar();

        JMenu mainMenu = new JMenu("Main");
        menuBar.add(mainMenu);

        windowMenu = new JMenu("Windows");
        menuBar.add(windowMenu);
        JMenuItem tileWindows = new JMenuItem("Tile vertically");
        tileWindows.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tile(mdi, interpreters, 1, interpreters.size());
            }
        });
        windowMenu.add(tileWindows);
        JMenuItem tileWindowsHorizontally = new JMenuItem("Tile horizontally");
        tileWindowsHorizontally.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tile(mdi, interpreters, interpreters.size(), 1);
            }
        });
        windowMenu.add(tileWindowsHorizontally);
        JMenuItem tileWindowsGrid = new JMenuItem("Tile as grid");
        tileWindowsGrid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int size = interpreters.size();
                int n = (int) Math.ceil(Math.sqrt(size));
                int m = (int) Math.ceil((double)size / n);

                if (n == m) {
                    int waste = n*m - size;
                    if (waste > 0) {
                        n = n - 1;
                        m = m + 1;
                    }
                }
                if (n > m) {
                    int tmp = n;
                    n = m;
                    m = tmp;
                }
                tile(mdi, interpreters, n, m);
            }
        });
        windowMenu.add(tileWindowsGrid);
        windowMenu.add(new JSeparator(JSeparator.HORIZONTAL));

        interpreters = new ArrayList<InternalInterpreterFrame>();

        newInterpreter = new JMenuItem("New interpreter");
        newInterpreter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = counter.incrementAndGet();
                String name = "Window " + index;
                final Interpreter interpreter = new Interpreter(kahlua.getPlatform(), kahlua.getEnvironment());
                final InternalInterpreterFrame frame = new InternalInterpreterFrame(interpreter, name);

                frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
                frame.addInternalFrameListener(new InternalFrameAdapter() {

                    @Override
                    public void internalFrameClosing(InternalFrameEvent e) {
                        interpreters.remove(frame);
                        windowMenu.remove(frame.getItem());
                        frame.dispose();
                    }
                });

                interpreters.add(frame);

                final JMenuItem item = new JMenuItem(name);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frame.moveToFront();
                        frame.setVisible(true);
                    }
                });
                frame.setMenuItem(item);
                windowMenu.add(item);


                mdi.add(frame);
                frame.setSize(600, 400);
                frame.getContentPane().add(interpreter);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        item.doClick();
                    }
                });

            }
        });
        mainMenu.add(newInterpreter);

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

		appendWelcome();
		appendKeybindings();

    }


	public void appendKeybindings() {
		getWindow(0).getInterpreter().getTerminal().appendInfo("Useful shortcuts:\n" +
				"Ctrl-enter -- execute script\n" +
				"Ctrl-space -- autocomplete global variables\n" +
				"Ctrl-p -- show definition (if available)\n" +
				"Ctrl-up/down -- browse input history\n" +
				""
		);
	}

	public void appendWelcome() {
		getWindow(0).getInterpreter().getTerminal().appendInfo("Welcome to the Kahlua interpreter\n");
	}

    private static void tile(JDesktopPane mdi,
                             List<InternalInterpreterFrame> interpreters,
                             int numX, int numY) {
        if (numX == 0 || numY == 0) {
            return;
        }
        Rectangle bounds = mdi.getBounds();
        int heightPerFrame = (int) (bounds.getHeight() / numX);
        int widthPerFrame = (int) (bounds.getWidth() / numY);

        int x = 0;
        int y = 0;
        for (InternalInterpreterFrame window : interpreters) {
            try {
                window.setMaximum(false);
            } catch (PropertyVetoException e1) {
                e1.printStackTrace();
            }
            window.setBounds(x, y, widthPerFrame, heightPerFrame);
            window.setVisible(true);
            x += widthPerFrame;
            if (x + widthPerFrame > bounds.getWidth()) {
                x = 0;
                y += heightPerFrame;
                if (y + heightPerFrame > bounds.getHeight()) {
                    y = 0;
                }
            }
        }
    }

    public InternalInterpreterFrame getWindow(int index) {
        return interpreters.get(index);
    }

	public JMenuItem getExit() {
		return exit;
	}
}
