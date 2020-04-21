package de.leonso.xfl.evaluators;

import de.leonso.core.UtilsString;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class HtmlDecodeEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public HtmlDecodeEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data data = expression.getElement(0).evaluate(rti);
		Object value = data.getValue();
		if (value instanceof String) {
			Data res = new Data(expression, rti);
			String convert = UtilsString.htmlToText((String) value);
			res.assignValue(convert);
			return res;
		}
		throw new IllegalArgumentException("@HtmlDecode: String expected, " + value.getClass().getName() + " found");
	}

}
