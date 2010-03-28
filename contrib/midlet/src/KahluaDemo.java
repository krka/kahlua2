/*
 Copyright (c) 2008 Kristofer Karlsson <kristofer.karlsson@gmail.com>

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

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.MathLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaPrototype;
import se.krka.kahlua.vm.LuaState;


public class KahluaDemo extends MIDlet implements Runnable, ItemStateListener {
	private String[] options = {"/guess", "/primes", "/quizgame", "Quit"};
	                        
	private LuaState state;
	private StringItem stringItem;
	private ChoiceGroup choices;

	public KahluaDemo() {
		state = new LuaState(System.out);
		
		state.getEnvironment().rawset("query", new JavaFunction() {
			public int call(LuaCallFrame callFrame, int nArguments) {
				BaseLib.luaAssert(nArguments >= 3, "not enough args");
				String[] options = new String[nArguments - 2];
				for (int i = 2; i < nArguments; i++) {
					options[i - 2] = BaseLib.rawTostring(callFrame.get(i)); 
				}
				String response = query(BaseLib.rawTostring(callFrame.get(0)), BaseLib.rawTostring(callFrame.get(1)), options);
				callFrame.push(response.intern());
				return 1;
			}
		});
		
		Form form = new Form("Kahlua Demo");
		
		stringItem = new StringItem("", "");
		form.append(stringItem);
		choices = new ChoiceGroup("Options", ChoiceGroup.MULTIPLE);
		choices.setDefaultCommand(new Command("OK", Command.OK, 1));
		form.setItemStateListener(this);
		
		form.append(choices);
		Display.getDisplay(this).setCurrent(form);
		
		new Thread(this).start();
	}

	public void run() {
		try {
			doRun();
		} catch (Exception e) {
		} finally {
			notifyDestroyed();
		}
	}
	private void doRun() throws IOException {
		while (true) {
			String response = query("", "Please choose a game", options);
			if (response.equals("Quit")) {
				return;
			} else {
				// The system needs to decide which game to load.
				stringItem.setText("Loading bytecode...");
				LuaClosure callback = state.loadByteCodeFromResource(response, state.getEnvironment());
				state.call(callback, null, null, null);
			}
		}
	}

	// Query operations
	private Object mutex = new Object();
	private String response;
	private String query(String label, String text, String[] options) {
		stringItem.setLabel(label);
		stringItem.setText(text);
		
		for (int i = 0; i < options.length; i++) {
			choices.append(options[i], null);
		}
		while (response == null) {
			synchronized (mutex) {
				try {
					mutex.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		try {
			return response;
		} finally {
			response = null;
		}
	}
	
	public void itemStateChanged(Item arg0) {
		synchronized (mutex) {
			if (response == null) {
				for (int i = choices.size() - 1; i >= 0; i--) {
					if (response == null && choices.isSelected(i)) {
						response = choices.getString(i); 
					}
					choices.delete(i);
				}
			}
			mutex.notifyAll();
		}
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}
	protected void pauseApp() {
	}
	protected void startApp() throws MIDletStateChangeException {
	}
}

