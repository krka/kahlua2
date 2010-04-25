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

package se.krka.kahlua.j2se.interpreter.autocomplete;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

class StringRef {
    private final JTextComponent component;
    private int start;
    private int len;

    public StringRef(JTextComponent component) {
        this.component = component;
    }

    public void set(int start, int len) {
        this.start = start;
        this.len = len;
    }

    public void clear() {
        set(0, 0);
    }

    public void increaseLength(int toAdd) {
        int max = component.getText().length() - start;
        len = Math.min(max, len + toAdd);
    }

    public void decreaseLength(int toRemove) {
        len = Math.max(0, len - toRemove);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return start + len;
    }

    public String toString() {
        try {
            return component.getText(start, len);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}
