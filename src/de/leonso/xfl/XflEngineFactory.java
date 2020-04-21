package de.leonso.xfl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import de.leonso.core.notes.LINotesFactoryProvider;

public class XflEngineFactory implements ScriptEngineFactory, Serializable {
	private static final long serialVersionUID = 1L;

	private LINotesFactoryProvider factoryProvider = null;

	public XflEngineFactory() {
	}

	public XflEngineFactory(LINotesFactoryProvider factoryProvider) {
		this.factoryProvider = factoryProvider;
	}

	@Override
	public String getEngineName() {
		return "XflEngine";
	}

	@Override
	public String getEngineVersion() {
		return "1.0";
	}

	@Override
	public List<String> getExtensions() {
		return null;
	}

	@Override
	public String getLanguageName() {
		return "XFL";
	}

	@Override
	public String getLanguageVersion() {
		return "1.0";
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		String ret = obj;
		ret += "." + m + "(";
		for (int i = 0; i < args.length; i++) {
			ret += args[i];
			if (i < args.length - 1) {
				ret += ";";
			}
		}
		ret += ");";
		return ret;
	}

	@Override
	public List<String> getMimeTypes() {
		return null;
	}

	@Override
	public List<String> getNames() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("xfl");
		return ret;
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return "@Print(" + toDisplay + ");";
	}

	/**
	 * Returns the value of an attribute whose meaning may be implementation-specific. Keys for which the value is defined in all implementations are:
	 * 
	 * ScriptEngine.ENGINE ScriptEngine.ENGINE_VERSION ScriptEngine.NAME ScriptEngine.LANGUAGE ScriptEngine.LANGUAGE_VERSION
	 * 
	 * The values for these keys are the Strings returned by getEngineName, getEngineVersion, getName, getLanguageName and getLanguageVersion respectively
	 */
	@Override
	public Object getParameter(String key) {
		if (ScriptEngine.ENGINE.equals(key)) {
			return getEngineName();
		} else if (ScriptEngine.ENGINE_VERSION.equals(key)) {
			return getEngineVersion();
		} else if (ScriptEngine.NAME.equals(key)) {
			return getNames();
		} else if (ScriptEngine.LANGUAGE.equals(key)) {
			return getLanguageName();
		} else if (ScriptEngine.LANGUAGE_VERSION.equals(key)) {
			return getLanguageVersion();
		}
		return null;
	}

	@Override
	public String getProgram(String... arg0) {
		return null;
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new XflEngine(factoryProvider);
	}

}
