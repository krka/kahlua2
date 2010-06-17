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

public class CompletionItem implements Comparable<CompletionItem> {
    private final String text;
    private final String value;
    private int score;

    public CompletionItem(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public void updateScore(String userInput) {
        score = calculateScore(userInput);
    }

    private int calculateScore(String userInput) {
        int totalScore = 1;
        String lowerText = text.toLowerCase();
        String lowerInput = userInput.toLowerCase();
        int runningIndex = 0;
        for (int pos = 0; pos < lowerInput.length(); pos++) {
            char realC = userInput.charAt(pos);
            
            int index;
            if (Character.isUpperCase(realC)) {
                index = text.indexOf(realC, runningIndex);
            } else {
                char c = lowerInput.charAt(pos);
                index = lowerText.indexOf(c, runningIndex);
            }
            if (index == -1) {
                return 0;
            }
            totalScore += 1000 - (pos - index);
            runningIndex = index + 1;
        }
        return totalScore;
    }

    @Override
    public String toString() {
        return text + ": " + value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(CompletionItem o) {
        int scoreDifference = getScore() - o.getScore();
        if (scoreDifference != 0) {
            return scoreDifference;
        }
        return text.compareTo(o.text);
    }

    public int getScore() {
        return score;
    }
}
