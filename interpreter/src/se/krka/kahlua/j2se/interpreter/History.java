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

package se.krka.kahlua.j2se.interpreter;

import java.util.Stack;

public class History {
    private final Stack<String> historyBack = new Stack<String>();
    private final Stack<String> historyForward = new Stack<String>();
    private String current = "";

    public History() {
    }


    public void add(String text) {
        if (validLine(text)) {
            if (!current.equals(text) && validLine(current)) {
                historyBack.push(current);
                current = "";
            }
            historyBack.push(text);
        }
    }

    private boolean validLine(String text) {
        return text.trim().length() != 0;
    }

    public void moveBack(InputTerminal input) {
        move(input, historyForward, historyBack);
    }

    public void moveForward(InputTerminal input) {
        move(input, historyBack, historyForward);
    }

    private void move(InputTerminal input, Stack<String> sink, Stack<String> source) {
        String newText = source.isEmpty() ? "" : source.pop();
        String curText = input.getText();
        if (validLine(curText)) {
            sink.push(curText);
        }
        input.setText(newText);
        current = newText;
    }
}
