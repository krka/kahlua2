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

package se.krka.kahlua.integration.expose;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;

import se.krka.kahlua.converter.LuaConversionError;
import se.krka.kahlua.converter.LuaConverterManager;
import se.krka.kahlua.integration.expose.caller.Caller;
import se.krka.kahlua.integration.processor.LuaClassDebugInformation;
import se.krka.kahlua.integration.processor.LuaMethodDebugInformation;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;

/**
 * Various variants:
 *
 * Object method or static (from lua) function:
 * --------------------------------
 * obj:method() -> method.invoke(obj, args)
 * obj.method() -> method.invoke(null, args)
 *
 * Multiple return values or not:
 * ------------------------------
 * local a, b, c = obj:method() -> obj = args[0]; rv = args[1]; method.invoke(obj, args[2:n])
 * local a = obj:method() -> obj = args[0]; method.invoke(obj, args[1:n])
 *
 * Varargs or not:
 * ---------------
 * obj:method(a, b, c, [d, e, f]) -> varargs = {d, e, f}; method.invoke(obj, args[1:3] + {varargs}) 
 *
 */
public class LuaJavaInvoker implements JavaFunction {
	private final LuaJavaClassExposer exposer;
	private final LuaConverterManager manager;
	private final Class<?> clazz;
	private final String name;
	private final Caller caller;

	private final Class<?>[] parameterTypes;
	private final int numMethodParams;

    private final Class<?> varargType;
    private final boolean hasSelf;
    private final boolean needsReturnValues;
    private final boolean hasVarargs;


    public LuaJavaInvoker(LuaJavaClassExposer exposer, LuaConverterManager manager, Class<?> clazz, String name, Caller caller) {
		this.exposer = exposer;
		this.manager = manager;
		this.clazz = clazz;
		this.name = name;
		this.caller = caller;

		this.parameterTypes = caller.getParameterTypes();
        this.varargType = caller.getVarargType();
        this.hasSelf = caller.hasSelf();
        this.needsReturnValues = caller.needsMultipleReturnValues();
        this.hasVarargs = caller.hasVararg();
		this.numMethodParams = parameterTypes.length + toInt(needsReturnValues) + toInt(hasVarargs);
	}

    private int toInt(boolean b) {
        return b ? 1 : 0;
    }

    public int call(LuaCallFrame callFrame, int nArguments) {
        final Object[] params = new Object[numMethodParams];

        int javaParamCounter = 0;
        int luaArgCounter = 0;

        // First handle the self argument
        int selfDecr = toInt(hasSelf);
        final Object self;
        if (hasSelf) {
            if (nArguments <= 0) {
                BaseLib.fail(syntaxErrorMessage("Expected a method call but got a function call."));
                return 0;
            }
            self = callFrame.get(0);
            luaArgCounter++;
        } else {
            self = null;
        }

		ReturnValues returnValues = new ReturnValues(manager, callFrame);

        // Then handle the returnvalues parameter
        if (needsReturnValues) {
            params[javaParamCounter] = returnValues;
            javaParamCounter++;
        }

        // Then handle regular arguments
        if (nArguments - luaArgCounter < parameterTypes.length) {
            int expected = parameterTypes.length;
            int got = nArguments - selfDecr;

            String errorMessage = "Expected " + expected + " arguments but got " + got + ".";
            errorMessage = syntaxErrorMessage(errorMessage);
            BaseLib.fail(errorMessage);
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Object o = callFrame.get(luaArgCounter + i);
            params[javaParamCounter + i] = convert(luaArgCounter + i - selfDecr, o, parameterTypes[i]);
        }
        javaParamCounter += parameterTypes.length;
        luaArgCounter += parameterTypes.length;

        // Finally handle varargs
        if (hasVarargs) {
            int numVarargs = nArguments - luaArgCounter;
            if (numVarargs < 0) {

            }
            Object[] varargs = (Object[]) Array.newInstance(varargType, numVarargs);
            for (int i = 0; i < numVarargs; i++) {
                varargs[i] = convert(luaArgCounter + i - selfDecr, callFrame.get(luaArgCounter + i), varargType);
            }
            params[javaParamCounter] = varargs;
            javaParamCounter++;
            luaArgCounter += numVarargs;
        }

        try {
            caller.call(self, returnValues, params);
            return returnValues.getNArguments();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (LuaConversionError e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private Object convert(int parameterIndex, Object o, Class<?> parameterType) {
        try {
            return manager.fromLuaToJava(o, parameterType);
        } catch (LuaConversionError luaConversionError) {
            throw newError(parameterIndex, luaConversionError);
        }
    }

    private String syntaxErrorMessage(String errorMessage) {
        String syntax = getFunctionSyntax();
        if (syntax != null) {
            errorMessage += " Correct syntax: " + syntax;
        }
        return errorMessage;
    }

	private RuntimeException newError(int i, Exception e) {
		int argumentIndex = i + 1;
		String errorMessage = e.getMessage() + " at argument #" + argumentIndex;
		String argumentName = getParameterName(i);
		if (argumentName != null) {
			errorMessage += ", " + argumentName;
		}
		return new RuntimeException(errorMessage);
	}

	private String getFunctionSyntax() {
		LuaMethodDebugInformation methodDebug = getMethodDebugData();
		if (methodDebug != null) {
			return methodDebug.getLuaDescription();
		}
		return null;
	}


	public LuaMethodDebugInformation getMethodDebugData() {
		LuaClassDebugInformation debugInformation = exposer.getDebugdata(clazz);
		if (debugInformation == null) {
			return null;
		}
		LuaMethodDebugInformation methodDebug = debugInformation.methods.get(name);
		return methodDebug;
	}

	private String getParameterName(int i) {
		LuaMethodDebugInformation methodDebug = getMethodDebugData();
		if (methodDebug != null) {
			return methodDebug.getParameterName(i);
		}
		return null;
	}

	public String toString() {
		return name;
	}
}

