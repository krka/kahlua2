/*
 Copyright (c) 2009 Kristofer Karlsson <kristofer.karlsson@gmail.com>

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

package se.krka.kahlua.integration.expose.caller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import se.krka.kahlua.integration.expose.ReturnValues;
import se.krka.kahlua.integration.processor.DescriptorUtil;

/** @exclude */
public class ConstructorCaller extends AbstractCaller {

	private final Constructor<?> constructor;

	public ConstructorCaller(Constructor<?> constructor) {
        super(constructor.getParameterTypes());
		this.constructor = constructor;
        constructor.setAccessible(true);
        if (needsMultipleReturnValues()) {
            throw new RuntimeException("Constructor can not return multiple values");
        }
	}
	
	@Override
	public void call(Object self, ReturnValues rv, Object[] params) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        rv.push(constructor.newInstance(params));
	}

	@Override
	public boolean hasSelf() {
		return false;
	}

    @Override
    public String getDescriptor() {
        return DescriptorUtil.getDescriptor(constructor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstructorCaller that = (ConstructorCaller) o;

        if (!constructor.equals(that.constructor)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return constructor.hashCode();
    }
}
