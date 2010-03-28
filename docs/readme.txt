=Contents=
<wiki:toc max_depth="2" />

Kahlua is a Virtual Machine together with a standard library,
all implemented in Java. It tries to emulate Lua as much as possible,
while still reusing as much as possible from Java. Nothing has been cloned or
ported directly from Lua, instead it's done by reverse engineering the
features that exists in Lua, mostly by using the Lua reference manual and PiL
as reference.

The target platform for Kahlua is J2ME (CLDC 1.1), which is commonly
included in mobile phones.
Everything in CLDC 1.1 is also included in standard Java so this will
run in most Java environments.

Kahlua can easily be integrated with a J2ME project - 
see the example midlet in the distribution.
The Java source code is just one class file and about 130 lines
of easy to read Java.

Kahlua may also be used for J2SE- or J2EE projects, but you would need to
use a stand alone lua compiler, since Kahlua lacks one.
This reduces the usefulness of Kahlua on J2SE environments.

=Goals=
The Kahlua project has the following goals (in no particular order):
  * Small class file footprint
  * Fast runtime of the most common operations
  * Same behaviour as standard Lua
  * Must be able to run on CLDC 1.1
  * Compact and non-redundant source
  * Maintain a large test base and high test coverage.

=License=
Kahlua is distributed under the MIT licence which is the same as standard Lua
which means you can
pretty much use it in any way you want.
However, I would very much appreciate bug reports, bug fixes, optimizations
or simply any good idea that might improve Kahlua.

=Major changes since Kahlua release 2009-04-26=
  * Forked the LuaJ compiler and included it by default
  * Added J2SE Util - simple utility classes for converting between types, and exposing methods from Java to Lua.

=Major changes since Kahlua release 2009-02-11=
  * Added support for using the LuaJ compiler.

=Major changes since Kahlua release 2008-10-11=
  * Strings used to be interned everywhere, but this has been changed for three reasons:
    # Always interning strings is a large strain on the permgen heap space which may be limited in size. It may even be dangerous to exceed the permgen limit on J2ME devices.
    # Taking advantage of string equality as identity did not give significant enough performance increase to motivate the harder semantics.
    # It was hard to use Java API, since you had to take responsibility for interning all strings that may lead into Lua.
  
    This means that you:
    # must stop assuming that strings from Lua have been interned and can be compared for equality by using identity.
    # should stop interning strings that you send to Lua.
  
  * Environments are stored per `LuaThread`, not `LuaState`. This means that you can't access `LuaState.environment` any more - however, there is now a convenience method `LuaState.getEnvironment()` that gets the environment from the current thread.

