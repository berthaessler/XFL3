package de.leonso.xfl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import de.leonso.core.notes.api.DocumentWrapper;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;

public class GlobalVarStore extends AbstractVarStore {
	private static final long serialVersionUID = 1L;

	public GlobalVarStore(XflEngine engine) {
		super(engine);
	}

	@Override
	Document createVarStoreDoc() throws Exception {
		Database currentDatabase = engine.getCurrentDatabase();
		Document doc = currentDatabase.createDocument();
		if (doc instanceof DocumentWrapper) {
			// temporäre Items nicht cachen. Performancekiller!
			((DocumentWrapper) doc).setSkipItemCache(true);
		}
		return doc;
	}

	public void removeAllNonGlobalTempItemsXXX() {
		Map<String, Data> allGlobalVars = engine.getAllGlobalVars();
		List<String> usedItems = new ArrayList<String>();
		Set<Entry<String, Data>> entrySet = allGlobalVars.entrySet();
		for (Entry<String, Data> entry : entrySet) {
			Data value = entry.getValue();
			if (value.getType().equals(DataType.ITEM_VAR)) {
				usedItems.add(value.getText());
			}
		}

		try {
			Vector<Item> items = getDoc().getItems();
			for (Item item : items) {
				String name = item.getName();
				if (!usedItems.contains(name)) {
					item.remove();
				}
			}
		} catch (NotesException e) {
		}
	}

}
