package de.leonso.xfl;

import javax.script.ScriptEngineManager;

import de.leonso.core.notes.LINotesFactoryProvider;

public class XflEngineManager extends ScriptEngineManager {

	public XflEngineManager(LINotesFactoryProvider factoryProvider) {
		registerEngineName("xfl", new XflEngineFactory(factoryProvider));
	}

	public XflEngineManager(ClassLoader loader) {
		super(loader);
	}

}
