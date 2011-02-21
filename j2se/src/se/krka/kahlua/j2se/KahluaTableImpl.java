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

package se.krka.kahlua.j2se;

import se.krka.kahlua.vm.*;

import java.util.Iterator;
import java.util.Map;

public class KahluaTableImpl implements KahluaTable {
    private final Map<Object, Object> delegate;
    private KahluaTable metatable;

    public KahluaTableImpl(Map<Object, Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setMetatable(KahluaTable metatable) {
        this.metatable = metatable;
    }

    @Override
    public KahluaTable getMetatable() {
        return metatable;
    }

    @Override
    public void rawset(Object key, Object value) {
        if (value == null) {
            delegate.remove(key);
            return;
        }
        delegate.put(key, value);
    }

    @Override
    public Object rawget(Object key) {
        if (key == null) {
            return null;
        }
        return delegate.get(key);
    }

    @Override
    public void rawset(int key, Object value) {
        rawset(KahluaUtil.toDouble(key), value);
    }

    @Override
    public Object rawget(int key) {
        return rawget(KahluaUtil.toDouble(key));
    }

    @Override
    public int len() {
        return KahluaUtil.len(this, 0, 2 * delegate.size());
    }

    @Override
    public KahluaTableIterator iterator() {
        final Iterator<Map.Entry<Object,Object>> iterator = delegate.entrySet().iterator();
        return new KahluaTableIterator() {
            private Object curKey;
            private Object curValue;

            @Override
            public int call(LuaCallFrame callFrame, int nArguments) {
                if (advance()) {
                    return callFrame.push(getKey(), getValue());
                }
                return 0;
            }

            @Override
            public boolean advance() {
                if (iterator.hasNext()) {
                    Map.Entry<Object, Object> value = iterator.next();
                    curKey = value.getKey();
                    curValue = value.getValue();
                    return true;
                }
                curKey = null;
                curValue = null;
                return false;
            }

            @Override
            public Object getKey() {
                return curKey;

            }

            @Override
            public Object getValue() {
                return curValue;
            }
        };

    }

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public void wipe() {
		delegate.clear();
	}

	@Override
    public String toString() {
        return "table 0x" + System.identityHashCode(this);
    }
}
