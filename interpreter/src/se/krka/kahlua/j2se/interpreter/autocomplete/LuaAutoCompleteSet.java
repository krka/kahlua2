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

import java.util.ArrayList;
import java.util.List;

public class LuaAutoCompleteSet implements CharacterSet {
    @Override
    public boolean isValid(char c) {
        return isIdentifier(c) || isSeparator(c);
    }

    private boolean isIdentifier(char c) {
        return Character.isLetter(c) || Character.isDigit(c) || c == '_';
    }

    @Override
    public boolean isSeparator(char c) {
        return c == '.' || c == ':';
    }

    @Override
    public List<String> split(String s) {
        List<String> ret = new ArrayList<String>();
        int pos = 0;
        while (true) {
            int nextPos = findNext(s, ":.", pos);
            if (nextPos == -1) {
                ret.add(s.substring(pos));
                return ret;
            } else {
                ret.add(s.substring(pos, nextPos));
                pos = nextPos + 1;
            }
        }
    }

    private int findNext(String s, String chars, int pos) {
        int nextPos = -1;
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            int tmp = s.indexOf(c, pos);
            if (tmp != -1) {
                if (nextPos == -1) {
                    nextPos = tmp;
                } else {
                    nextPos = Math.min(nextPos, tmp);
                }
            }
        }
        return nextPos;
    }

}
