package de.leonso.xfl.evaluators;

import de.leonso.core.UtilsString;
import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

/**
 * Ersetzt alle Sonderzeichen durch "normale" Buchstaben
 */
public class NormalizeExceptUmlautEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public NormalizeExceptUmlautEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data data = expression.getElement(0).evaluate(rti);
		Object value = data.getValue();
		if (value instanceof String) {
			Data res = new Data(expression, rti);
			String convert = UtilsString.normalizeExceptUmlaut((String) value);
			res.assignValue(convert);
			return res;
		}
		throw new IllegalArgumentException("@NormalizeExceptUmlaut: String expected, " + value.getClass().getName() + " found");
	}

}
