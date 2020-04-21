package de.leonso.xfl;

import lotus.domino.Document;

public class LocalVarStore extends AbstractVarStore {
	private static final long serialVersionUID = 1L;

	public LocalVarStore(XflEngine engine) {
		super(engine);
	}

	@Override
	Document createVarStoreDoc() throws Exception {
		GlobalVarStore gvs = engine.getGlobalVarStore();
		return gvs.getDoc();
	}

}
