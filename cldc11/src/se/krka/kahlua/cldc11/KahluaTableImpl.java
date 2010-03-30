/*
Copyright (c) 2007-2008 Kristofer Karlsson <kristofer.karlsson@gmail.com>
Portions of this code Copyright (c) 2007 Andre Bogus <andre@m3n.de>

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
package se.krka.kahlua.cldc11;

import se.krka.kahlua.stdlib.TableLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;

import java.util.Enumeration;
import java.util.Hashtable;


public final class KahluaTableImpl implements KahluaTable {
    private final Hashtable delegate = new Hashtable();
	private KahluaTable metatable;

    public void setMetatable(KahluaTable metatable) {
        this.metatable = metatable;
    }

    public KahluaTable getMetatable() {
        return metatable;
    }

    public void rawset(Object key, Object value) {
        if (value == null) {
            delegate.remove(key);
            return;
        }
        delegate.put(key, value);
    }

    public Object rawget(Object key) {
        return delegate.get(key);
    }

    public void rawset(int key, Object value) {
        rawset(KahluaUtil.toDouble(key), value);
    }

    public Object rawget(int key) {
        return rawget(KahluaUtil.toDouble(key));
    }

    public int len() {
        return TableLib.len(this, 0, delegate.size());
    }

    public JavaFunction iterator() {
        final Enumeration enumeration = delegate.keys();
        return new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
                if (enumeration.hasMoreElements()) {
                    Object key = enumeration.nextElement();
                    Object value = delegate.get(key);
                    return callFrame.push(key, value);
                }
                return 0;
            }
        };
    }
}
