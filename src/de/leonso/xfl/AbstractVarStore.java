package de.leonso.xfl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;

public abstract class AbstractVarStore implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<String, Object> varBackup = new HashMap<String, Object>();
	private transient Document varStoreDoc = null;

	protected final XflEngine engine;

	public AbstractVarStore(XflEngine engine) {
		this.engine = engine;
	}

	abstract Document createVarStoreDoc() throws Exception;

	public void clear() {
		if (varBackup.size() > 0) {
			try {
				Document varStoreDoc = getDoc();
				Set<Entry<String, Object>> entrySet = varBackup.entrySet();
				for (Entry<String, Object> entry : entrySet) {
					String fieldName = entry.getKey();
					varStoreDoc.removeItem(fieldName);
					// Data.releaseItemName(fieldName);
					// wir warten auf Finalizer von Data
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			varBackup.clear();
		}
	}

	public Document getDoc() {
		if (varStoreDoc == null) {
			try {
				varStoreDoc = createVarStoreDoc();
				Set<Entry<String, Object>> entrySet = varBackup.entrySet();
				for (Entry<String, Object> entry : entrySet) {
					Object value = entry.getValue();
					String fieldName = entry.getKey();
					if (value == null) {
						// ERROR
						// den konkreten Fehler können wir leider nicht reaktivieren
						// generieren wir also einen beliebigen, z.B. Div0
						engine.getSession().evaluate("FIELD " + fieldName + ":=1/0;{}", varStoreDoc);
					} else {
						varStoreDoc.replaceItemValue(fieldName, value);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return varStoreDoc;
	}

	public void saveItem(String itemName) throws NotesException {
		Document doc = getDoc();
		if (doc.hasItem(itemName)) {
			Item item = doc.getFirstItem(itemName);
			if (Item.ERRORITEM == item.getType()) {
				varBackup.put(itemName, null); // als Fehler speichern
			} else {
				varBackup.put(itemName, doc.getItemValue(itemName));
			}
		} else {
			varBackup.remove(itemName);
		}
	}

	public void removeItem(String itemName) throws NotesException {
		getDoc().removeItem(itemName);
		varBackup.remove(itemName);
	}

	public void replaceItemValue(String itemName, Object value) throws Exception {
		getDoc().replaceItemValue(itemName, value);
		varBackup.put(itemName, value);
	}

}
