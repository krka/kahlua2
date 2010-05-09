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

import jsyntaxpane.Lexer;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class VoidLexer implements Lexer {
    SyntaxDocument doc;
    private List<Token> newTokens = new ArrayList<Token>();

    @Override
    public synchronized void parse(Segment segment, int i, List<Token> tokens) {
        Iterator<Token> iter = doc.getTokens(i, segment.getEndIndex());
        Token prev = null;
        while (iter.hasNext()) {
            Token token = iter.next();
            prev = handlePrev(tokens, prev, token);
        }
        for (Token token : newTokens) {
            prev = handlePrev(tokens, prev, token);
        }
        prev = handlePrev(tokens, prev, null);
        newTokens.clear();
    }

    private Token handlePrev(List<Token> tokens, Token prev, Token current) {
        if (prev == null) {
            return current;
        } else if (current != null && current.type == prev.type && prev.end() == current.start) {
            return merge(prev, current);
        } else {
            tokens.add(prev);
            return current;
        }
    }

    private Token merge(Token prev, Token token) {
        prev = new Token(prev.type, prev.start, prev.length + token.length, prev.pairValue);
        return prev;
    }

    public void setNewTokens(List<Token> newTokens, int startAt) {
        for (Token newToken : newTokens) {
            this.newTokens.add(new Token(
                    newToken.type,
                    newToken.start + startAt,
                    newToken.length,
                    newToken.pairValue
                    ));
        }
    }
}
