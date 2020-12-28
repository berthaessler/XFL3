package de.leonso.xfl.evaluators;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Utils;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.EvaluationException;
import de.leonso.xfl.exceptions.ObjectVariableNotSetException;
import lotus.domino.Base;
import lotus.domino.Document;

/**
 * Evaluator fuer Punkt (Objektaufruf) z.B. object.method()
 * 
 * @author Bert
 * 
 */
public class DotEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public DotEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Object o = expression.getElement(0).evaluate(context).getObject();
		if (o == null) {
			throw new ObjectVariableNotSetException(getEngine(), expression);
		}
		Expression methodExp = expression.getElement(1);
		String title = methodExp.getTitle();
		ArrayList<Expression> params = methodExp.getElements();
		List<Class<?>> classList = new ArrayList<Class<?>>();
		List<Object> argsList = new ArrayList<Object>();
		for (Expression ex : params) {
			Object value = ex.evaluate(context).getValue();
			argsList.add(value);
			Class<? extends Object> c = value == null ? null : value.getClass();
			// if (c.equals(Object[].class)) {
			// // Mehrfachwerte kommen gern als "Object"
			// if (value instanceof List) {
			// c = List.class;
			// }
			// }
			classList.add(c);
		}
		// Class<?>[] argsClasses = (Class<?>[]) classList.toArray();
		Class<?>[] argsClasses = new Class<?>[classList.size()];
		for (int i = 0; i < classList.size(); i++) {
			argsClasses[i] = classList.get(i);
		}
		Method method = Utils.getMethod(o.getClass(), title, argsClasses);
		if (method == null) {
			// vielleicht liegt es an den numerischen Werten
			// Die kommen meist als Double an
			boolean found = false;
			for (int i = 0; i < classList.size(); i++) {
				Class<?> class1 = classList.get(i);
				if (class1.equals(Double.class)) {
					argsClasses[i] = Number.class;
					argsList.set(i, (Number) argsList.get(i));
					found = true;
				}
			}
			if (found) {
				method = Utils.getMethod(o.getClass(), title, argsClasses);
			}
		}

		if (method == null) {
			if (o instanceof Base) { // doc.UniversalID soll gehen
				method = Utils.getMethod(o.getClass(), "get" + title, argsClasses);
				if (method == null) {
					method = Utils.getMethod(o.getClass(), "is" + title, argsClasses);
				}
				if (method == null) {
					if (o instanceof Document && params.size() <= 1) { // doc.Feldname
						Document doc = (Document) o;
						Vector<?> value = doc.getItemValue(title);
						Data res = new Data(expression, context);
						if (value.size() == 0) {
							res.assignValue("");
						} else if (value.size() == 1) {
							res.assignValue(value.get(0));
						} else {
							res.assignValue(new ArrayList<Object>(value));
						}
						return res;
					}
				}
			}
		}
		if (method == null) {
			String signature = "";
			for (Class<?> c : argsClasses) {
				if (signature.length() > 0) {
					signature += ", ";
				}
				signature += c.getName();
			}
			throw new EvaluationException(getEngine(), expression, getEngine().getMessage("method.not.found", title, signature, o.getClass().getName()));
		}
		Object[] args = argsList.toArray();
		Object inv = Utils.invokeMethod(o, method, args);
		Data res = new Data(expression, context);
		res.assignValue(inv);
		return res;
	}

}
