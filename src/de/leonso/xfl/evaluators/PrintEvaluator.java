package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class PrintEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public PrintEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data data = expression.getElement(0).evaluate(rti);
		Object value = data.getValue();
		String txt;
		// Achtung @Error liefert null
		if (value == null) {
			txt = "#ERROR";
		} else {
			txt = value.toString();
		}
		getEngine().log(txt);
		return data;
	}

}
