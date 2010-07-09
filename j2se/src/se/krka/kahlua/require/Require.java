package se.krka.kahlua.require;

import se.krka.kahlua.vm.*;
import se.krka.kahlua.luaj.compiler.LuaCompiler;

import java.io.Reader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class Require implements JavaFunction {
    private enum State {
        LOADING,
        LOADED,
        BROKEN;
    };

    private static class Result {
        public final String errorMessage;
        public final State state;

        private Result(String errorMessage, State state) {
            this.errorMessage = errorMessage;
            this.state = state;
        }

        public static final Result LOADING = new Result(null, State.LOADING);
        public static final Result LOADED = new Result(null, State.LOADED);
        public static Result error(String s) {
            return new Result(s, State.BROKEN);
        }
    }

    public void install(KahluaTable environment) {
        environment.rawset("require", this);
        environment.rawset(this, new HashMap<String, Result>());
    }

    private final LuaSourceProvider luaSourceProvider;

    public Require(LuaSourceProvider luaSourceProvider) {
        this.luaSourceProvider = luaSourceProvider;
    }

    public int call(LuaCallFrame callFrame, int nArguments) {
        KahluaTable env = callFrame.getEnvironment();
        Map<String, Result> states = (Map<String, Result>) callFrame.getThread().tableGet(env, this);

		String path = KahluaUtil.getStringArg(callFrame, 1, "require");

        Result result = states.get(path);
        if (result == null) {
            setState(states, path, Result.LOADING);
            
            Reader source = luaSourceProvider.getLuaSource(path);
            if (source == null) {
                error(states, path, "Does not exist: " + path);
            }

            try {
                LuaClosure luaClosure = LuaCompiler.loadis(source, path, env);
                setState(states, path, Result.LOADING);
                callFrame.getThread().call(luaClosure, null, null, null);
                setState(states, path, Result.LOADED);

                return 0;
            } catch (IOException e) {
                error(states, path, "Error in: " + path + ": " + e.getMessage());
            } catch (RuntimeException e) {
                String msg = "Error in: " + path + ": " + e.getMessage();
                setState(states, path, Result.error(msg));
                throw new RuntimeException(msg, e);
            }
        }
        if (result == Result.LOADING) {
            error(states, path, "Circular dependency found for: " + path);
        }

        if (result.state == State.BROKEN) {
            KahluaUtil.fail(result.errorMessage);
        }

        return 0;
    }

    private void error(Map<String, Result> states, String path, String s) {
        setState(states, path, Result.error(s));
        KahluaUtil.fail(s);
    }

    private void setState(Map<String, Result> requireLookuptable, String path, Result result) {
        requireLookuptable.put(path, result);
    }
}
