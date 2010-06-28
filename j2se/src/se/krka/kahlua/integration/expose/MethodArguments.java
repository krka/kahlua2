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

package se.krka.kahlua.integration.expose;

/** @exclude */
public class MethodArguments {
    private ReturnValues returnValues;
    private Object self;
    private Object[] params;
    private String failure;

    public MethodArguments(int numMethodParams) {
        params = new Object[numMethodParams];
    }

    public ReturnValues getReturnValues() {
        return returnValues;
    }

    public Object getSelf() {
        return self;
    }

    public Object[] getParams() {
        return params;
    }

    public void fail(String failure) {
        this.failure = failure;
    }

    public void setSelf(Object self) {
        this.self = self;
    }

    public String getFailure() {
        return failure;
    }

    public void setReturnValues(ReturnValues returnValues) {
        this.returnValues = returnValues;
    }

    public void assertValid() {
        if (!isValid()) {
            throw new RuntimeException(failure);
        }
    }

    public boolean isValid() {
        return failure == null;
    }

}
