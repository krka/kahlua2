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
import jsyntaxpane.SyntaxStyle;
import jsyntaxpane.SyntaxStyles;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class JSyntaxUtil {
    private static boolean initialized;

    public static void setup() {
        if (initialized) {
            return;
        }
        initialized = true;

        // Hack to set jsyntax default color
        try {
            for (Field field : SyntaxStyles.class.getDeclaredFields()) {
                if (field.getName().equals("DEFAULT_STYLE")) {
                    field.setAccessible(true);
                    field.set(null, new SyntaxStyle(Color.WHITE, false, false));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        DefaultSyntaxKit.initKit();
    }

    public static void installSyntax(final JEditorPane textPane) {
        textPane.setContentType("text/kahlua");
    }
}
