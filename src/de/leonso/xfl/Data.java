package de.leonso.xfl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.script.ScriptException;

import de.leonso.core.notes.api.DocumentWrapper;
import de.leonso.xfl.exceptions.EvaluationException;
import de.leonso.xfl.exceptions.NotesFormulaEvaluationException;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class Data implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Expression expression;
	private final XflEngine engine;
	private final Context context;

	private DataType type = DataType.NULL;
	private String text = null;
	private final List<Data> children = new ArrayList<Data>();

	private static int lastItemNumber = 0;
	private static List<String> releasedItems = new ArrayList<String>();

	public Data(Expression parent, Context context) {
		this.expression = parent;
		this.engine = parent.getEngine();
		this.context = context;
	}

	public Data(XflEngine parent) {
		this.expression = null;
		this.engine = parent;
		this.context = null;
	}

	private final static byte ACCESS_OPTION_GET_FISRT_AND_REMOVE = 1;
	private final static byte ACCESS_OPTION_ADD = 2;
	private final static byte ACCESS_OPTION_ADD_ALL = 3;

	@SuppressWarnings("unchecked")
	private synchronized static String accessReleasedItemList(byte function, Object name) {
		if (ACCESS_OPTION_GET_FISRT_AND_REMOVE == function) {
			String res = releasedItems.get(0);
			releasedItems.remove(0);
			return res;
		} else if (ACCESS_OPTION_ADD == function) {
			releasedItems.add((String) name);
		} else if (ACCESS_OPTION_ADD_ALL == function) {
			releasedItems.addAll((Collection<? extends String>) name);
		}
		return null;
	}

	private void setItemName() {
		if (releasedItems.size() > 0) {
			// text = releasedItems.get(0);
			// releasedItems.remove(0);
			text = accessReleasedItemList(ACCESS_OPTION_GET_FISRT_AND_REMOVE, null);
		} else {
			lastItemNumber++;
			text = "i" + lastItemNumber;
		}
	}

	public Data convertToVarItem() throws Exception {

		switch (type) {

		case NULL: // nix zu tun
		case ITEM_VAR: // nix zu tun
			break;

		case CODE_BOTH:
		case CODE_VAR:
			String code = text;
			setItemName();
			type = DataType.ITEM_VAR;
			Session session = getSession();
			Vector<?> ev = null;
			try {
				AbstractVarStore varStore = (context == null) ? engine.getGlobalVarStore() : context.getVarStore();
				Document doc = varStore.getDoc();
				ev = session.evaluate("FIELD " + text + " := " + code + "; 0", doc);
				varStore.saveItem(text);
			} catch (Throwable e) {
				throw new NotesFormulaEvaluationException(engine, expression, e);
			}
			if (ev.isEmpty()) {
				throw new Exception("Data.macro.error");
			}
			removeChildren();
			break;

		case CODE_REF:
			code = text;
			Document refdoc = context.getRefDoc();
			LocalVarStore varStore = context.getVarStore();
			if (refdoc != null) {
				setItemName();
				type = DataType.ITEM_VAR;
				session = getSession();
				ev = session.evaluate("FIELD $XFLTEMPITEM := " + code + "; 0", refdoc);
				if (ev.isEmpty()) {
					throw new Exception("Data.macro.error");
				}
				Item it = refdoc.getFirstItem("$XFLTEMPITEM");
				it.copyItemToDocument(varStore.getDoc(), text);
				varStore.saveItem(text);
				it.remove();
				it.recycle();
				removeChildren();
			} else {
				// z.B. bei @DbLookup in XFLInit, in diesem Fall einfach auf XFLVarStore anwenden
				setItemName();
				type = DataType.ITEM_VAR;
				session = getSession();
				ev = session.evaluate("FIELD " + text + " := " + code + "; 0", varStore.getDoc());
				if (ev.isEmpty()) {
					throw new Exception("Data.macro.error");
				}
				varStore.saveItem(text);
				removeChildren();
			}
			break;

		case UNAVAILABLE:
			text = "$XFL_UN_AV";
			break;

		case ITEM_REF:
			Item it = context.getRefDoc().getFirstItem(text);
			varStore = context.getVarStore();
			it.copyItemToDocument(varStore.getDoc(), text);
			varStore.saveItem(text);
			it.remove();
			type = DataType.ITEM_VAR;
			break;

		case OBJECT:
			// Object lassen wir ohne Fehler zu, auch wenn's nichts bringt
			break;

		}
		return this;
	}

	private Session getSession() throws Exception {
		return engine.getSession();
	}

	public void convertToVar() throws Exception {

		switch (type) {

		case CODE_VAR: // stimmt schon
		case ITEM_VAR: // stimmt schon
		case OBJECT: // ignorieren
			break;

		case CODE_BOTH:
			type = DataType.CODE_VAR;
			break;

		case CODE_REF:
		case UNAVAILABLE:
		case ITEM_REF:
			convertToVarItem();
			break;

		default:
			throw new Exception("Data.unknown.type");
		}
	}

	public Data convertToRef() throws Exception {
		if (context.getRefDoc() == null) {
			return this;
		}
		switch (type) {

		case CODE_REF:
		case ITEM_REF:
		case OBJECT:
			break;

		case CODE_BOTH:
			type = DataType.CODE_REF;
			break;

		case CODE_VAR:
			convertToVarItem();
		case ITEM_VAR:
			// nicht einfach den Typ aendern, sondern Kopie erzeugen.
			// sonst wuerde z.B. bei Zugriff auf lokale Variable diese in
			// XFLRefDoc kopiert und danach Typ ITEM_REF haben
			// (bloed bei naechstem Zugriff und altes Item wird nicht geloescht)
			Data newData = new Data(expression, context);
			LocalVarStore varStore = context.getVarStore();
			Item it = varStore.getDoc().getFirstItem(text);
			newData.text = "$XFL_" + text;
			it = it.copyItemToDocument(context.getRefDoc(), newData.text);
			it.setSaveToDisk(false);
			newData.type = DataType.ITEM_REF;
			return newData;

		case UNAVAILABLE:
			type = DataType.CODE_REF;
			text = "$UN_AV";
			break;

		default:
			throw new Exception("Data.unknown.type");
		}

		return this;
	}

	public Data addChild(Data a) throws Exception {
		if (type == DataType.NULL) {
			type = a.type;
		} else if (a.type == DataType.NULL) {
			return a; // leeres Element ignorieren
		} else if (type != a.type) {
			// Typen angleichen
			DataType r = type.getMixedType(a.type);
			if (r != DataType.NULL) {
				// einer der beiden Typen war CODE_BOTH, der andere auch
				// CODE_XXX
				if (type == r) {
					if (a.type == DataType.ITEM_VAR) {
						// wenn a als Item registriert ist, muss das am Ende freigegeben werden
						// Typ nicht ändern, nur anhängen
						children.add(a);
					} else {
						a.type = r;
					}
				} else {
					for (Data child : children) {
						// wenn a als Item registriert ist, muss das am Ende freigegeben werden
						// Typ nicht ändern
						if (child.type != DataType.ITEM_VAR) {
							child.type = r;
						}
					}
					children.add(a);
					type = r;
				}
			} else if (type == DataType.ITEM_VAR) {
				a.convertToVar();
			} else if (type == DataType.CODE_REF) {
				// direkt mit dem Dokument arbeiten. alle Variablen als Felder übergeben
				// Kopie von a anlegen und mit dieser weiterarbeiten
				// v2.87 das Original darf nicht weggeworfen werden. sonst wird
				// der Name freigegeben und schon neu belegt, obwohl die Kopie
				// (gleicher Name) noch verwendet wird
				a = a.convertToRef();
				children.add(a);
			} else if ((type == DataType.CODE_VAR && a.type == DataType.CODE_REF) || (a.type == DataType.CODE_VAR && type == DataType.CODE_REF)) {
				// einer von beiden CODE_BOTH
				a.convertToVar();
			} else if (type == DataType.ITEM_VAR) {
				for (Data child : children) {
					child.convertToVar();
				}
				convertToVar();
			} else if (a.type == DataType.UNAVAILABLE) {
				a.convertToVar();
			} else { // z.B. Object und Code
				// nicht kompatibel, aber keinen Fehler ausloesen.
				// Kann passieren bei XFL_IF, z.B. @If(a=1; OBJECT o:=
				// @CreateObject(...)...)
			}
		}
		return a;
	}

	private synchronized void removeItem(Document doc, String itemName) throws NotesException {
		if (doc != null) {
			// doc.removeItem(itemName); das scheint kritisch zu sein
		}
	}

	protected void finalize() throws Throwable {
		try {
			// wäre zu schön, hat aber komische Nebeneffekte: doc ist dann
			// plötzlich recycled
			// close();
			if (type == DataType.ITEM_VAR) {
				if (text.length() > 0) {
					// System.out.println("DELETE " + text);
					// engine.addUnusedVarStoreItem(text);

					engine.registerUnusedItem(text);

					// releaseItemName(text);
					// DocumentWrapper refDoc = context.getRefDoc();
					// removeItem(refDoc, text);
				}
			} else if (type == DataType.OBJECT) {
				// engine.addUnusedObjectStoreItem(text);
				releaseItemName(text);
			} else if (type == DataType.ITEM_REF) {
				if (text.length() > 0) {
					DocumentWrapper refDoc = context.getRefDoc();
					removeItem(refDoc, text);
				}
			}
		} catch (Exception ignore) {
		}
	};

	// nicht mehr verwenden, wir lassen das den GC machen
	// private void close() throws Exception {
	// if (type == DataType.ITEM_VAR) {
	// if (text.length() > 0) {
	// XflVarStore varStore = getVarStore();
	// Document doc = varStore.getDoc();
	// if (doc != null) {
	// varStore.removeItem(text);
	// releaseItemName();
	// }
	// }
	// } else if (type == DataType.ITEM_REF) {
	// if (text.length() > 0) {
	// if (getRefDoc() != null) {
	// getRefDoc().removeItem(text);
	// }
	// }
	// } else if (type == DataType.OBJECT) {
	// if (getObjectStore().containsKey(text)) {
	// getObjectStore().remove(text);
	// releaseItemName();
	// }
	// }
	// removeChildren();
	// }

	protected Map<String, Object> getObjectStore() {
		return engine.getObjectStore();
	}

	private void removeChildren() {
		children.clear();
	}

	protected static void releaseItemName(String name) {
		accessReleasedItemList(ACCESS_OPTION_ADD, name);
	}

	protected static void releaseItemNames(List<String> names) {
		accessReleasedItemList(ACCESS_OPTION_ADD_ALL, names);
	}

	public void assignItem(Item item) throws Exception {
		setItemName();
		AbstractVarStore varStore = context == null ? engine.getGlobalVarStore() : context.getVarStore();
		item.copyItemToDocument(varStore.getDoc(), text);
		varStore.saveItem(text);
		type = DataType.ITEM_VAR;
	}

	public void assignValue(Object v) throws Exception {
		// wenn echte Objekte hier reinkommen, dann ggfs. umlenken
		Object korr = v;
		if (v == null) {
			type = DataType.UNAVAILABLE;
			return;
		} else if (v instanceof Boolean) {
			type = DataType.CODE_BOTH;
			text = (Boolean) v ? "@True" : "@False";
			return;
		} else if (v instanceof String) {
		} else if (v instanceof Number) {
		} else if (v instanceof Vector) {
		} else if (v instanceof List) {
			korr = new Vector<Object>((List<?>) v);
		} else if (v instanceof Object[]) { // vielleicht doch eine Textliste
			Object[] oarr = (Object[]) v;
			Class<? extends Object> cl = null;
			boolean mixed = false;
			for (Object object : oarr) {
				Class<? extends Object> oClass = object.getClass();
				if (cl == null) {
					cl = oClass;
				} else {
					if (oClass != cl) {
						mixed = true;
						break;
					}
				}
			}
			if (mixed) {
				assignObject(v);
				return;
			} else {
				korr = new Vector<Object>(Arrays.asList((Object[]) v));
			}
		} else {
			assignObject(v);
			return;
		}
		setItemName();
		// TODO
		// If Datatype(v) = 11 Then ' v2.89
		// v = Cint(v)
		// End If
		AbstractVarStore varStore;
		if (context == null) {
			varStore = engine.getGlobalVarStore();
		} else {
			varStore = context.getVarStore();
		}
		try {
			varStore.replaceItemValue(text, korr);
			type = DataType.ITEM_VAR;
		} catch (Exception cannotStoreObjectToItem) {
			assignObject(v); // Original speichern
		}
	}

	public void assignObject(Object v) throws Exception {
		setItemName();
		getObjectStore().put(text, v);
		type = DataType.OBJECT;
	}

	public Object getValue() throws ScriptException {
		try {
			if (type == DataType.CODE_BOTH | type == DataType.CODE_REF | type == DataType.CODE_VAR) {
				convertToVarItem();
			}
			if (type == DataType.ITEM_VAR) {
				Document varStoreDoc = context == null ? engine.getGlobalVarStore().getDoc() : context.getVarStore().getDoc();
				Vector<?> temp = null;
				try {
					temp = varStoreDoc.getItemValue(text);
				} catch (Exception e) {
					if (varStoreDoc.hasItem(text)) {
						// koennte ein @Error-Item sein
						if (varStoreDoc.getFirstItem(text).getType() == Item.ERRORITEM) {
							return null;
						}
					}
					throw e;
				}
				if (temp.size() == 0) { // leerer String kommt als leeres Array
					return "";
				} else if (temp.size() == 1) {
					Object v = temp.get(0);
					if (v instanceof Double) {
						// Zahlen kommen immer als Double
						// ggfs. in Integer konvertieren
						Double n = (Double) v;
						if (Double.compare(n, n.intValue()) == 0) {
							v = n.intValue();
						}
					}
					return v;
				} else {
					// TODO ggfs. auch von Double in Integer konvertieren
					return temp.toArray();
				}
			} else if (type == DataType.ITEM_REF) {
				if (context.getRefDoc() == null) {
					return null;
				} else {
					Vector<?> temp = context.getRefDoc().getItemValue(text);
					if (temp.size() == 1) {
						Object v = temp.get(0);
						if (v instanceof Double) {
							// Zahlen kommen immer als Double
							// ggfs. in Integer konvertieren
							Double n = (Double) v;
							if (Double.compare(n, n.intValue()) == 0) {
								v = n.intValue();
							}
						}
						return v;
					} else {
						// TODO ggfs. auch von Double in Integer konvertieren
						return temp.toArray();
					}
				}
			} else if (type == DataType.UNAVAILABLE) {
				return ""; // das ergibt nix
			} else if (type == DataType.NULL) {
				// ohne Wert. z.B. nach @Deletedocument
				return "";
			} else if (type == DataType.OBJECT) {
				return getObject();
			} else {
				throw new Exception("Data.type.mismatch");
			}
		} catch (EvaluationException e) {
			throw e;
		} catch (Exception e) {
			throw new ScriptException(e);
		}
	}

	public Object getObject() {
		return getObjectStore().get(text);
	}

	public Item getItem() throws Exception {
		convertToVarItem();
		// TODO wer holt das Item? ggfs. Änderungen registrieren
		return type == DataType.ITEM_VAR ? (context == null ? engine.getGlobalVarStore().getDoc() : context.getVarStore().getDoc()).getFirstItem(text) : null;
	}

	public boolean isTrue() throws Exception {
		if (type == DataType.CODE_BOTH || type == DataType.CODE_REF || type == DataType.CODE_VAR) {
			if (text.equalsIgnoreCase("@true")) {
				return true;
			}
			if (text.equalsIgnoreCase("@false")) {
				return false;
			}
		}
		Item item = getItem();
		if (item == null) {
			return false;
		}
		if (item.getType() == Item.ERRORITEM) {
			return false;
		}
		if (item.getType() != Item.NUMBERS) {
			return false;
		}
		if (item.getValueInteger() != 0) {
			return true;
		}
		return false;
	}

	public boolean isError() throws Exception {
		Item item = getItem();
		if (item == null) {
			return false;
		}
		if (item.getType() == Item.ERRORITEM) {
			return true;
		}
		return false;
	}

	public void changeFormula(String f) {
		switch (type) {
		case ITEM_VAR:
			type = DataType.CODE_VAR;
			break;
		case ITEM_REF:
			type = DataType.CODE_REF;
			break;
		case UNAVAILABLE:
			type = DataType.CODE_BOTH;
			break;
		case OBJECT:
			type = DataType.CODE_BOTH;
			break;
		default:
			break;
		}
		text = f;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public void setText(String value) {
		this.text = value;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		String v = "";
		try {
			if (getType() == DataType.ITEM_VAR) {

				Object value = getValue();
				if (value instanceof Object[]) {
					v += "[";
					boolean trenn = false;
					Object[] l = (Object[]) value;
					for (Object object : l) {
						v += (trenn ? ", " : "") + object.toString();
						trenn = true;
					}
					v += "]";
				} else {
					v = value.toString();
				}
			} else if (getType() == DataType.OBJECT) {
				v = getValue().toString();
			}

		} catch (Throwable e) {
			v = "<error.resolving.value>";
		}
		return getText() + " (" + getType() + ")" + ("".equals(v) ? "" : ", value: " + v);
	}

	/**
	 * weist einen konstanten String zu.<br/>
	 * Die Länge entscheidet, ob der String als Formelausdruck weitergegeben werden kann oder als Variable.
	 * 
	 * @param type
	 * @param code
	 */
	public void assignCodeConstant(DataType type, String code) {
		// session.evaluate() kann nicht endlos lange Formeln verarbeiten
		if (code.length() > XflEngine.MAX_CODE_LENGTH) {
			// Stringbegrenzer und Maskierungen entfernen
			String value = extractValueFromCode(code);
			try {
				assignValue(value);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		} else {
			setText(code);
			setType(type);
		}
	}

	/**
	 * extrahiert den Wert eines Textes aus einer Codekonstanten.<br/>
	 * "Hallo" -> Hallo, {Hallo} -> Hallo, "Hall\"o" -> Hall"o, ...
	 * 
	 * @param code
	 * @return
	 */
	private String extractValueFromCode(String code) {
		String z1 = code.substring(0, 1);
		String value;
		if ("\"".equals(z1)) {
			value = code.substring(1, code.length() - 1);
			value = value.replace("\\\"", "\"");
		} else if ("{".equals(z1)) {
			value = code.substring(1, code.length() - 1);
			value = value.replace("\\{", "\"");
			value = value.replace("\\}", "\"");
		} else {
			// ???
			value = code;
		}
		return value;
	}

	/**
	 * gibt zurück, ob dieses Ergebnis zu einer RTI gehört. Falls das der Fall ist, wird das Objekt mit der RTI zusammen aufgeräumt.
	 * 
	 * @return
	 */
	public boolean hasRunTimeInstance() {
		return context != null;
	}

}
