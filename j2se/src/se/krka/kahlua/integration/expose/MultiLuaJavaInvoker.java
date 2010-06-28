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

import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** @exclude */
public class MultiLuaJavaInvoker implements JavaFunction {
    private final List<LuaJavaInvoker> invokers = new ArrayList<LuaJavaInvoker>();
    private static final Comparator<? super LuaJavaInvoker> COMPARATOR = new Comparator<LuaJavaInvoker>() {
        @Override
        public int compare(LuaJavaInvoker o1, LuaJavaInvoker o2) {
            return o2.getNumMethodParams() - o1.getNumMethodParams();
        }
    };

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        MethodArguments methodArguments = null;
        for (LuaJavaInvoker invoker : invokers) {
            methodArguments = invoker.prepareCall(callFrame, nArguments);
            if (methodArguments.isValid()) {
                return invoker.call(methodArguments);
            }
        }
        if (methodArguments != null) {
            methodArguments.assertValid();
        }
        throw new RuntimeException("No implementation found");
    }

    public void addInvoker(LuaJavaInvoker invoker) {
        if (!invokers.contains(invoker)) {
            invokers.add(invoker);
            Collections.sort(invokers, COMPARATOR);
        }
    }

    public List<LuaJavaInvoker> getInvokers() {
        return invokers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiLuaJavaInvoker that = (MultiLuaJavaInvoker) o;

        if (!invokers.equals(that.invokers)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return invokers.hashCode();
    }
}
