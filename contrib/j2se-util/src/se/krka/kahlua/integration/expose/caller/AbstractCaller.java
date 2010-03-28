package se.krka.kahlua.integration.expose.caller;

import se.krka.kahlua.integration.expose.ReturnValues;

import java.util.Arrays;

public abstract class AbstractCaller implements Caller {
    protected final Class<?>[] parameters;
    protected final boolean needsMultipleReturnValues;

    protected final Class<?> varargType;

    protected AbstractCaller(Class<?>[] parameters) {
        boolean needsMultipleReturnValues = false;
        Class<?> varargType = null;
        if (parameters.length > 0) {
            Class<?> firstType = parameters[0];
            if (firstType == ReturnValues.class) {
                needsMultipleReturnValues = true;
            }

            Class<?> lastType = parameters[parameters.length - 1];
            if (lastType.isArray()) {
                varargType = lastType.getComponentType();
            }
        }

        this.needsMultipleReturnValues = needsMultipleReturnValues;
        this.varargType = varargType;
        this.parameters = Arrays.copyOfRange(parameters, needsMultipleReturnValues ? 1 : 0, parameters.length - (varargType == null ? 0 : 1));
    }

    @Override
    public final Class<?>[] getParameterTypes() {
        return parameters;
    }

    @Override
    public final Class<?> getVarargType() {
        return varargType;
    }

    @Override
    public final boolean hasVararg() {
        return varargType != null;
    }

    @Override
    public final boolean needsMultipleReturnValues() {
        return needsMultipleReturnValues;
    }
}
