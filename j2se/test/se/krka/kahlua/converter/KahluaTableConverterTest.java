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

package se.krka.kahlua.converter;

import org.junit.Test;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.vm.Platform;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class KahluaTableConverterTest {
    @Test
    public void testCollection() {
        LuaConverterManager manager = new LuaConverterManager();
        Platform platform = new J2SEPlatform();
        new KahluaTableConverter(platform).install(manager);

        Collection result = manager.fromLuaToJava(platform.newTable(), Collection.class);
        assertNotNull(result);
    }

    @Test
    public void testList() {
        LuaConverterManager manager = new LuaConverterManager();
        Platform platform = new J2SEPlatform();
        new KahluaTableConverter(platform).install(manager);

        Collection result = manager.fromLuaToJava(platform.newTable(), List.class);
        assertNotNull(result);
    }

}
