package de.leonso.xfl.evaluators;

import java.lang.reflect.Method;
import java.util.Vector;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.SubType;
import de.leonso.xfl.Type;
import de.leonso.xfl.Utils;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.EvaluationException;
import de.leonso.xfl.exceptions.SubscriptOutOfRangeException;
import lotus.domino.Document;
import lotus.domino.Item;

public class AssignmentEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public AssignmentEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {

		Data res;

		Expression e1 = expression.getElement(0);
		String temp = e1.getTitle();
		// evtl. Alias aufloesen, Achtung, wenn Alias schon mal definiert wurde!
		// Dann neu definieren, nicht aufloesen!
		XflEngine engine = getEngine();

		if (!e1.isAlias() && engine.isAliasDefined(temp)) {
			temp = engine.getAlias(temp);
		}
		if (e1.isField()) {
			Document refDoc = rti.getRefDoc();
			if (e1.isDefault() && refDoc.hasItem(temp)) {
				// DEFAULT braucht hier nix zu tun, weil schon Wert existiert
				// Was gibt DEFAULT zurueck? Unter R6 nix, DEFAULT kann nicht
				// verschachtelt werden, a := DEFAULT b := "1";
				res = new Data(expression, rti);
				res.assignItem(refDoc.getFirstItem(temp));
				// damit sind wir besser als Notes ;-)
			} else {
				res = expression.getElement(1).evaluate(rti);
				if (res.getType() == DataType.UNAVAILABLE) {
					refDoc.removeItem(temp);
				} else {
					XflEngine.setFieldValue(refDoc, temp, res.getItem());
				}
			}
		} else if (e1.isDefinedFunction()) {// isField
			// verschiede Signaturen zulassen
			engine.setUDF(temp, expression);
			res = new Data(expression, rti);
			res.setType(DataType.UNAVAILABLE);
		} else if (e1.isGlobal()) {
			res = expression.getElement(1).evaluate(rti);
			if (e1.isObject() || res.getType() == DataType.OBJECT) {
				if (res.getType() == DataType.UNAVAILABLE) {
					if (engine.hasGlobalObject(temp)) {
						engine.removeGlobalObject(temp);
					}
				} else {
					engine.setGlobalObject(temp, res);
				}
			} else { // kein OBJECT, aber GLOBAL
				if (res.getType() == DataType.UNAVAILABLE) {
					if (engine.hasGlobalVar(temp)) {
						engine.removeGlobalVar(temp);
					}
				} else {
					res.convertToVarItem();
					engine.setGlobalVar(temp, res);
					// R6-kompatibel
				}
			}
		} else if (e1.isEnvironment()) {// isField
			res = expression.getElement(1).evaluate(rti);
			Object vTemp = res.getValue();
			engine.getSession().setEnvironmentVar(temp, vTemp);
			// R6-kompatibel
		} else if (e1.isObject()) { // isField
			res = expression.getElement(1).evaluate(rti);
			if (res.getType() == DataType.UNAVAILABLE) {
				if (rti.hasObject(temp)) {
					rti.removeObject(temp);
				}
			} else {
				rti.setObject(temp, res);
			}
		} else if (e1.getType() == Type.LIST_SUBSCRIPT) { // das kann SFL nicht
															// ;-)
			res = e1.getElement(0).evaluate(rti).convertToVarItem();
			@SuppressWarnings("unchecked")
			Vector<Object> values = res.getItem().getValues();
			Integer index1 = (Integer) e1.getElement(1).evaluate(rti).getValue();
			int i = index1 - 1;
			if (values.size() < i) {
				throw new SubscriptOutOfRangeException(getEngine(), expression, index1);
			}
			Object value = expression.getElement(1).evaluate(rti).getValue();
			values.set(i, value);
			res.assignValue(values);
			rti.setVar(res.getItem().getName(), res);
		} else if (e1.isAlias()) {
			engine.setAlias(temp, expression.getElement(1).getTitle());
			res = new Data(expression, rti);
			res.setType(DataType.UNAVAILABLE);
		} else if (e1.getSubType() == SubType.OPERATOR_DOT) { // o.method := x
			Object oTemp = e1.getElement(0).evaluate(rti).getObject();
			String method = e1.getElement(1).getTitle();
			Object v = expression.getElement(1).evaluate(rti).getValue();
			// wir versuchen es ueber den Setter
			// o.value := "xyz" -> o.setValue("xyz")
			Method m = Utils.getMethod(oTemp, "set" + method, v);
			if (m == null) {
				// vielleicht ist es sowas: item.isSummary := @False;
				if (method.toLowerCase().startsWith("is")) {
					method = "set" + method.substring(2);
					m = Utils.getMethod(oTemp, method, v);
				}
				if (m == null) {
					if (oTemp instanceof Document) { // doc.Feldname = "xyz"
														// soll gehen
						Document doc = (Document) oTemp;
						Object korr;
						if (v instanceof Object[]) {
							Vector<Object> vec = new Vector<Object>();
							Object[] oarr = (Object[]) v;
							for (Object object : oarr) {
								vec.add(object);
							}
							korr = vec;
						} else {
							korr = v;
						}
						Item item = doc.replaceItemValue(method, korr);
						res = new Data(expression, rti);
						res.assignObject(item);
						return res;
					}
					throw new EvaluationException(getEngine(), expression, engine.getMessage("method.not.found", method));
				}
			}
			Utils.invokeMethod(oTemp, m, v);
			res = new Data(expression, rti);
			res.assignValue(null);

		} else { // local var
			res = expression.getElement(1).evaluate(rti);
			if (res.getType() == DataType.UNAVAILABLE) {
				if (rti.hasVar(temp)) {
					rti.removeVar(temp);
				}
			} else {
				if (res.getType() == DataType.OBJECT) {
					rti.setObject(temp, res);
				} else {
					// Achtung! Kopie erzeugen!
					Data resOrg = res;
					res = new Data(expression, rti);
					res.assignItem(resOrg.getItem());
					rti.setVar(temp, res);
				}
			}
		}

		return res;
	}

}
