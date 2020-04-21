package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class DebugEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public DebugEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data res = expression.getElement(0).evaluate(rti);
		Number value = (Number) res.getValue();
		if (value.intValue() == 0) {
			getEngine().setDebugMode(false);
		} else {
			getEngine().setDebugMode(true);
		}
		return res;
	}

}
