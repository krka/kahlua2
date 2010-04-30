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
import javax.swing.text.Document;
import javax.swing.text.Segment;

public class WordFinder {
    private final Document document;
    private final Segment segment;
    private final CharacterSet characterSet;

    public WordFinder(Document document, CharacterSet characterSet) {
        this.document = document;
        this.characterSet = characterSet;
        segment = new Segment();
    }

    public synchronized Word findBackwards(int position) {
        try {
            updateSegment();
            int start = getStart(position);
            return new Word(document, start, position);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Word findBothWays(int position) {
        try {
            updateSegment();
            int start = getStart(position);
            int end = getEnd(position);
            return new Word(document, start, end);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Word findLastPart(Word word) {
        try {
            updateSegment();
            int start = getStartWithSeparator(word.getEnd());
            return new Word(document, start, word.getEnd());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public Word withoutLastPart(Word word) {
        try {
            updateSegment();
            int start = getStartWithSeparator(word.getEnd());
            return new Word(document, word.getStart(), start);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private int getEnd(int end) {
        while (end < document.getLength()) {
            if (!isValid(segment.charAt(end))) {
                return end;
            }
            end++;
        }
        return end;

    }

    private int getStart(int start) {
        while (start > 0) {
            start--;
            if (!isValid(segment.charAt(start))) {
                return start + 1;
            }
        }
        return start;
    }

    private int getStartWithSeparator(int start) {
        while (start > 0) {
            start--;
            char c = segment.charAt(start);
            if (!isValid(c) || isSeparator(c)) {
                return start + 1;
            }

        }
        return start;
    }

    private boolean isSeparator(char c) {
        return characterSet.isSeparator(c);
    }

    private boolean isValid(char c) {
        return characterSet.isValid(c);
    }

    private void updateSegment() throws BadLocationException {
        document.getText(document.getStartPosition().getOffset(), document.getEndPosition().getOffset(), segment);
    }
}
