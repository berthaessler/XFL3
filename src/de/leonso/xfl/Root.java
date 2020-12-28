package de.leonso.xfl;

import javax.script.ScriptException;

import de.leonso.core.notes.api.DocumentWrapper;

public class Root extends Expression {
	private static final long serialVersionUID = 1L;

	public static final String TITLE = "<root>";
	private final XflEngine engine;
	private final String code;

	public Root(XflEngine engine, String code) {
		super();
		this.engine = engine;
		this.code = code;
		this.setType(Type.ROOT);
		this.root = this;
	}

	// Lizenzcheck schon erfolgt?
	// private boolean checked = false;

	@Override
	public Data evaluate(Context context) throws ScriptException {
		DocumentWrapper refDoc = context.getRefDoc();
		boolean sameDoc = false;

		boolean globalDocWasSet = false;
		Object orgGlobalDoc = null; // koennte sogar abweichend von refDoc sein,
									// wenn die Formeln das überschrieben haben
		if (engine.hasGlobalObject("doc")) {
			orgGlobalDoc = engine.getGlobalObject("doc");
			globalDocWasSet = true;
			if (refDoc == orgGlobalDoc) {
				sameDoc = true;
			}
		}

		if (!sameDoc) {
			engine.setGlobalObject("doc", refDoc); // doc als globales Objekt
		}

		// bereitstellen
		Data result = super.evaluate(context);
		if (globalDocWasSet) {
			boolean resetGlobalDoc = true;
			if (sameDoc) {
				// vorsichtshalber noch einmal schauen, ob sich daran was geändert hat
				// falls das alte globale doc nicht geändert wurde und es dem aktuellen entspricht, kann Variable bleiben
				if (engine.hasGlobalObject("doc")) {
					Object test = engine.getGlobalObject("doc");
					if (test == orgGlobalDoc) {
						resetGlobalDoc = false;
					}
				}
			}
			if (resetGlobalDoc) {
				engine.setGlobalObject("doc", orgGlobalDoc);
			}
		} else {
			engine.removeGlobalObject("doc");
		}

		// aufräumen
		engine.removeUnusedItems();

		return result;
	}

	@Override
	public XflEngine getEngine() {
		return engine;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

}
