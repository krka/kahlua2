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

import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.expose.caller.Caller;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;

/**
 * This is a JavaFunction that is an adapter for Java methods.
 *
 * <pre>
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
 * </pre>
 */
public class LuaJavaInvoker implements JavaFunction {
	private final LuaJavaClassExposer exposer;
	private final KahluaConverterManager manager;
	private final Class<?> clazz;
	private final String name;
	private final Caller caller;

	private final Class<?>[] parameterTypes;
	private final int numMethodParams;

    private final Class<?> varargType;
    private final boolean hasSelf;
    private final boolean needsReturnValues;
    private final boolean hasVarargs;


    public LuaJavaInvoker(LuaJavaClassExposer exposer, KahluaConverterManager manager, Class<?> clazz, String name, Caller caller) {
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

    public MethodArguments prepareCall(LuaCallFrame callFrame, int nArguments) {
        MethodArguments methodArguments = new MethodArguments(numMethodParams);

        int javaParamCounter = 0;
        int luaArgCounter = 0;

        // First handle the self argument
        int selfDecr = toInt(hasSelf);
        if (hasSelf) {
            Object self = nArguments <= 0 ? null : callFrame.get(0);
            if (self == null || !clazz.isInstance(self)) {
                methodArguments.fail(syntaxErrorMessage("Expected a method call but got a function call."));
                return methodArguments;
            }
            methodArguments.setSelf(self);
            luaArgCounter++;
        }

		ReturnValues returnValues = new ReturnValues(manager, callFrame);
        methodArguments.setReturnValues(returnValues);
        // Then handle the returnvalues parameter
        if (needsReturnValues) {
            methodArguments.getParams()[javaParamCounter] = returnValues;
            javaParamCounter++;
        }

        // Then handle regular arguments
        if (nArguments - luaArgCounter < parameterTypes.length) {
            int expected = parameterTypes.length;
            int got = nArguments - selfDecr;

            String errorMessage = "Expected " + expected + " arguments but got " + got + ".";
            methodArguments.fail(syntaxErrorMessage(errorMessage));
            return methodArguments;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Object o = callFrame.get(luaArgCounter + i);
            int parameterIndex = luaArgCounter + i - selfDecr;
            Class<?> parameterType = parameterTypes[i];
            Object obj = convert(o, parameterType);
            if (o != null && obj == null) {
                methodArguments.fail(newError(parameterIndex, "No conversion found from " + o.getClass() + " to " + parameterType.getName()));
                return methodArguments;
            }
            methodArguments.getParams()[javaParamCounter + i] = obj;
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
                Object o = callFrame.get(luaArgCounter + i);
                int parameterIndex = luaArgCounter + i - selfDecr;
                Object obj = convert(o, varargType);
                varargs[i] = obj;
                if (o != null && obj == null) {
                    methodArguments.fail(newError(parameterIndex, "No conversion found from " + o.getClass() + " to " + varargType.getName()));
                    return methodArguments;
                }
            }
            methodArguments.getParams()[javaParamCounter] = varargs;
            javaParamCounter++;
            luaArgCounter += numVarargs;
        }

        return methodArguments;
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        MethodArguments methodArguments = prepareCall(callFrame, nArguments);
        methodArguments.assertValid();
        return call(methodArguments);
    }

    public int call(MethodArguments methodArguments) {
        try {
            ReturnValues returnValues = methodArguments.getReturnValues();
            caller.call(methodArguments.getSelf(), returnValues, methodArguments.getParams());
            return returnValues.getNArguments();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private Object convert(Object o, Class<?> parameterType) {
        if (o == null) {
            return null;
        }
        Object value = manager.fromLuaToJava(o, parameterType);
        return value;
    }

    private String syntaxErrorMessage(String errorMessage) {
        String syntax = getFunctionSyntax();
        if (syntax != null) {
            errorMessage += " Correct syntax: " + syntax;
        }
        return errorMessage;
    }

	private String newError(int i, String message) {
		int argumentIndex = i + 1;
		String errorMessage = message + " at argument #" + argumentIndex;
		String argumentName = getParameterName(i);
		if (argumentName != null) {
			errorMessage += ", " + argumentName;
		}
		return errorMessage;
	}

	private String getFunctionSyntax() {
		MethodDebugInformation methodDebug = getMethodDebugData();
		if (methodDebug != null) {
			return methodDebug.getLuaDescription();
		}
		return null;
	}


	public MethodDebugInformation getMethodDebugData() {
        ClassDebugInformation debugInformation = exposer.getDebugdata(clazz);
		if (debugInformation == null) {
			return null;
		}
        return debugInformation.getMethods().get(caller.getDescriptor());
	}

	private String getParameterName(int i) {
		MethodDebugInformation methodDebug = getMethodDebugData();
		if (methodDebug != null) {
			return methodDebug.getParameters().get(i).getName();
		}
		return null;
	}

	public String toString() {
		return name;
	}

    public int getNumMethodParams() {
        return numMethodParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LuaJavaInvoker that = (LuaJavaInvoker) o;

        if (!caller.equals(that.caller)) return false;
        if (!clazz.equals(that.clazz)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clazz.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + caller.hashCode();
        return result;
    }
}

