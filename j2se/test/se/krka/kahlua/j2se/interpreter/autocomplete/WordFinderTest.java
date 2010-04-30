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

import org.junit.Test;

import javax.swing.text.*;

import static org.junit.Assert.assertEquals;

public class WordFinderTest {

    @Test
    public void findSimpleWord() throws BadLocationException {
        AbstractDocument.Content content = new StringContent(0);
        content.insertString(0, " MyWord");
        Document document = new PlainDocument(content);
        CharacterSet characterSet = new LuaAutoCompleteSet();
        WordFinder finder = new WordFinder(document, characterSet);
        Word word = finder.findBackwards(5);
        assertEquals("MyWo", word.toString());
    }

    @Test
    public void findComplexWord() throws BadLocationException {
        AbstractDocument.Content content = new StringContent(0);
        content.insertString(0, " MyWord.Foo:Bar");
        Document document = new PlainDocument(content);
        CharacterSet characterSet = new LuaAutoCompleteSet();
        WordFinder finder = new WordFinder(document, characterSet);
        Word word = finder.findBackwards(5);
        assertEquals("MyWo", word.toString());
    }

    @Test
    public void findComplexWord2() throws BadLocationException {
        AbstractDocument.Content content = new StringContent(0);
        content.insertString(0, " MyWord.Foo:Bar");
        Document document = new PlainDocument(content);
        CharacterSet characterSet = new LuaAutoCompleteSet();
        WordFinder finder = new WordFinder(document, characterSet);
        Word word = finder.findBackwards(15);
        assertEquals("MyWord.Foo:Bar", word.toString());
    }

    @Test
    public void findComplexWordForward() throws BadLocationException {
        AbstractDocument.Content content = new StringContent(0);
        content.insertString(0, " MyWord.Foo:Bar    ");
        Document document = new PlainDocument(content);
        CharacterSet characterSet = new LuaAutoCompleteSet();
        WordFinder finder = new WordFinder(document, characterSet);
        Word word = finder.findBothWays(1);
        assertEquals("MyWord.Foo:Bar", word.toString());
    }

    @Test
    public void findComplexWordForward2() throws BadLocationException {
        AbstractDocument.Content content = new StringContent(0);
        content.insertString(0, " MyWord.Foo:Bar");
        Document document = new PlainDocument(content);
        CharacterSet characterSet = new LuaAutoCompleteSet();
        WordFinder finder = new WordFinder(document, characterSet);
        Word word = finder.findBothWays(5);
        assertEquals("MyWord.Foo:Bar", word.toString());
    }

    @Test
    public void noMatch() throws BadLocationException {
        AbstractDocument.Content content = new StringContent(0);
        content.insertString(0, "Test    \n");
        Document document = new PlainDocument(content);
        CharacterSet characterSet = new LuaAutoCompleteSet();
        WordFinder finder = new WordFinder(document, characterSet);
        Word word = finder.findBothWays(5);
        assertEquals("", word.toString());

    }

    @Test
    public void noMatch2() throws BadLocationException {
        AbstractDocument.Content content = new StringContent(0);
        content.insertString(0, "\n");
        Document document = new PlainDocument(content);
        CharacterSet characterSet = new LuaAutoCompleteSet();
        WordFinder finder = new WordFinder(document, characterSet);
        Word word = finder.findBothWays(0);
        assertEquals("", word.toString());
    }

    @Test
    public void simpleLastPart() throws BadLocationException {
        AbstractDocument.Content content = new StringContent(0);
        content.insertString(0, "    MyWord.Foo:Bar   \n");
        Document document = new PlainDocument(content);
        CharacterSet characterSet = new LuaAutoCompleteSet();
        WordFinder finder = new WordFinder(document, characterSet);
        Word word = finder.findBothWays(10);
        Word lastPart = finder.findLastPart(word);
        Word withoutLastPart = finder.withoutLastPart(word);
        assertEquals("Bar", lastPart.toString());
        assertEquals("MyWord.Foo:", withoutLastPart.toString());
    }

}
