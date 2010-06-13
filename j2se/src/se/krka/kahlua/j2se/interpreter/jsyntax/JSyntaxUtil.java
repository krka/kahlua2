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

package se.krka.kahlua.j2se.interpreter.jsyntax;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.SyntaxStyles;
import jsyntaxpane.components.LineNumbersRuler;
import jsyntaxpane.components.PairsMarker;
import jsyntaxpane.components.TokenMarker;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Properties;

/**
 * @exclude
 */
public class JSyntaxUtil {
    private static boolean initialized;

    public static void setup() {
        if (initialized) {
            return;
        }
        initialized = true;

        // Hack to set jsyntax default color
        Properties config = new Properties();
        config.put("DEFAULT", "0xffffff, 0");
        SyntaxStyles.getInstance().mergeStyles(config);

        DefaultSyntaxKit.initKit();
    }

    public static DefaultSyntaxKit installSyntax(final JEditorPane textPane, boolean highlight, final DefaultSyntaxKit kit) {
        Properties config = new Properties();
        config.put("CaretColor", "0xffffff");
        kit.setConfig(config);
        textPane.setEditorKit(kit);

        kit.deinstallComponent(textPane, LineNumbersRuler.class.getName());
        kit.deinstallComponent(textPane, TokenMarker.class.getName());

        if (highlight) {
            textPane.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    kit.installComponent(textPane, PairsMarker.class.getName());
                    kit.installComponent(textPane, TokenMarker.class.getName());
                }

                @Override
                public void focusLost(FocusEvent e) {
                    kit.deinstallComponent(textPane, PairsMarker.class.getName());
                    kit.deinstallComponent(textPane, TokenMarker.class.getName());
                }
            });
        }
        return kit;
    }
}
