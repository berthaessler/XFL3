package de.leonso.xfl.evaluators;

import java.util.List;
import java.util.Vector;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.SubscriptOutOfRangeException;

public class IndexEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public IndexEvaluator(XflEngine engine) {
		super(engine);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data dTemp = expression.getElement(0).evaluate(rti);
		Object value = dTemp.getValue();
		List<Object> list = null;
		if (value instanceof List<?>) {
			list = (List<Object>) value;
		} else if (value instanceof Object[]) {
			list = new Vector<Object>();
			Object[] array = (Object[]) value;
			for (Object object : array) {
				list.add(object);
			}
		} else {
			list = new Vector<Object>();
			if (value == null) {
				list.add("");
			} else {
				list.add(value);
			}
		}

		dTemp = expression.getElement(1).evaluate(rti);
		Object indexO = dTemp.getValue();
		Integer ix = ((Number) indexO).intValue();
		Data res = new Data(expression, rti);

		// das hier kann Fehler ausloesen.
		// v3.0.3: negative Angabe zaehlt von hinten
		if (ix < 0) {
			ix = list.size() + ix + 1;
		}
		if ((list.size() < ix) || (ix < 1)) {
			throw new SubscriptOutOfRangeException(getEngine(), expression, ix);
		}
		Object v = list.get(ix - 1); // erster Index 1
		res.assignValue(v);

		return res;

	}

}
