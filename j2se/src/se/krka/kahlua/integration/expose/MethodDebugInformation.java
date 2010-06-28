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

import java.util.List;

/** @exclude */
public class MethodDebugInformation {
    private final String luaName;
    private final boolean isMethod;
    private final List<MethodParameter> parameters;
    private final String returnType;
    private final String returnDescription;

    public MethodDebugInformation(
            String luaName,
            boolean isMethod,
            List<MethodParameter> parameters,
            String returnType,
            String returnDescription) {
        this.parameters = parameters;
        this.luaName = luaName;
        this.isMethod = isMethod;
        this.returnDescription = returnDescription;

        if (parameters.size() > 0 && parameters.get(0).getType().equals(ReturnValues.class.getName())) {
            returnType = "...";
            parameters.remove(0);
        }
        this.returnType = returnType;
    }

    public String getLuaName() {
        return luaName;
    }

    public String getLuaDescription() {
        String separator = isMethod ? "obj:" : "";
        String msg = TypeUtil.removePackages(returnType) + " " + separator + luaName + "(" + getLuaParameterList() + ")\n";
        if (getReturnDescription() != null) {
            msg += getReturnDescription() + "\n";
        }
        return msg;
    }

    public boolean isMethod() {
        return isMethod;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public String getReturnDescription() {
        return returnDescription;
    }

    public String getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return getLuaDescription();
    }

    private String getLuaParameterList() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (MethodParameter parameter : parameters) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            String type = TypeUtil.removePackages(parameter.getType());
            builder.append(type).append(" ").append(parameter.getName());
        }
        return builder.toString();
    }

    private String getParameterList() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (MethodParameter parameter : parameters) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(parameter.getType()).append(" ").append(parameter.getName());
        }
        return builder.toString();
    }

}
