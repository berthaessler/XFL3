package de.leonso.xfl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import de.leonso.core.exception.LException;

/**
 * Daten werden in Form von XflData-Objekten gehalten
 * 
 * @author Bert
 * 
 */
public class XflBindings implements Bindings, Serializable {
	private static final long serialVersionUID = 1L;

	// global vars
	private final Map<String, Data> globalVars = new HashMap<String, Data>();

	private final XflEngine engine;

	public XflBindings(XflEngine engine) {
		super();
		this.engine = engine;
	}

	@Override
	public String toString() {
		try {
			return super.toString();
		} catch (Throwable e) {
			return LException.getRootMessage(e);
		}
	}

	@Override
	public void clear() {
		globalVars.clear();
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		return globalVars.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return globalVars.size();
	}

	@Override
	public Collection<Object> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsKey(Object key) {
		String uKey = ((String) key).toUpperCase();
		return globalVars.containsKey(uKey) || engine.hasGlobalObject(uKey);
	}

	@Override
	public Object get(Object key) {
		try {
			return getData(key).getValue();
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * als XflData
	 * 
	 * @param key
	 * @return {@link Data}
	 */
	public Data getData(Object key) {
		try {
			Data data = globalVars.get(((String) key).toUpperCase());
			if (data == null) {
				data = engine.getGlobalObjectWrapper((String) key);
			}
			return data;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, Data> getAll() {
		return globalVars;
	}

	@Override
	public Object put(String name, Object value) {
		if (value instanceof Data) {
			Data data = (Data) value;
			// Achtung: Wenn data zu einer RuntimeInstanz gehört, dann müssen
			// wir hier eine Kopie erzeugen,
			// sonst wird data beim Beenden der RTI mit entsorgt
			if (data.hasRunTimeInstance()) {
				Data res = new Data(engine);
				try {
					res.assignItem(data.getItem());
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
				globalVars.put(name.toUpperCase(), res);
			} else {
				globalVars.put(name.toUpperCase(), data);
			}
			return value;
		} else {
			try {

				if (value instanceof Number) {
				} else if (value instanceof String) {
				} else {
					engine.setGlobalObject(name, value);
					return value;
				}

				Data res = new Data(engine);
				res.assignValue(value);
				globalVars.put(name.toUpperCase(), res);
				return res;

			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object remove(Object key) {
		return globalVars.remove(key);
	}

}
