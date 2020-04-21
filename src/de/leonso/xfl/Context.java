package de.leonso.xfl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import de.leonso.core.notes.api.DocumentWrapper;
import lotus.domino.Document;

public class Context implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Expression root;
	private DocumentWrapper refDoc;
	private LocalVarStore varStore;

	// local objects
	private final Map<String, Data> localObjects = new HashMap<String, Data>();

	// local vars
	private final Map<String, Data> localVars = new HashMap<String, Data>();

	Context(Expression tmpRoot, Document refDoc) {
		this.root = tmpRoot;
		XflEngine engine = getEngine();
		try {
			// wegen Security nicht die Klasse NotesDocument, sondern
			// unseren Wrapper nehmen
			this.refDoc = engine.getNotesFactory().wrapDocument(refDoc);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		varStore = new LocalVarStore(engine);
	}

	public XflEngine getEngine() {
		return root.getEngine();
	}

	public LocalVarStore getVarStore() {
		return varStore;
	}

	public Object evaluate() throws ScriptException {
		DocumentWrapper d = getRefDoc();
		if (d != null) {
			Data data = new Data(root.getEngine());
			try {
				data.assignObject(d);
			} catch (Exception ignore) {
			}
			setObject("doc", data);
		}
		Data res = root.evaluate(this);
		if (res == null) {
			return null;
		}
		Object value = res.getValue();
		return value;
	}

	public void close() {
		varStore.clear();
	}

	public DocumentWrapper getRefDoc() {
		return refDoc;
	}

	public void setRefDoc(DocumentWrapper doc) {
		refDoc = doc;
	}

	public boolean hasObject(String name) {
		return localObjects.containsKey(name.toUpperCase());
	}

	public Data getObject(String name) {
		return localObjects.get(name.toUpperCase());
	}

	public void setObject(String name, Data o) throws ScriptException {
		localObjects.put(name.toUpperCase(), o);
	}

	public void removeObject(String name) {
		localObjects.remove(name.toUpperCase());
	}

	public void setVar(String name, Data var) throws ScriptException {
		localVars.put(name.toUpperCase(), var);
	}

	public boolean hasVar(String name) {
		return localVars.containsKey(name.toUpperCase());
	}

	public Data getVar(String name) {
		return localVars.get(name.toUpperCase());
	}

	public Map<String, Data> getVarsMap() {
		return localVars;
	}

	public Map<String, Data> getObjectsMap() {
		return localObjects;
	}

	public void removeVar(String name) {
		localVars.remove(name.toUpperCase());
	}

	public void removeAllVars() {
		localVars.clear();
	}

}
