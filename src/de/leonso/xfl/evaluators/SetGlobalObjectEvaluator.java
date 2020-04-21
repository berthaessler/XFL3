package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class SetGlobalObjectEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public SetGlobalObjectEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {

		String var = (String) expression.getElement(0).evaluate(rti).getValue();
		XflEngine engine = getEngine();

		Data res = expression.getElement(1).evaluate(rti);

		if (res.getType() == DataType.UNAVAILABLE) {
			engine.removeGlobalObject(var);
		} else {
			engine.setGlobalObject(var, res);
		}

		return res;

	}

}
