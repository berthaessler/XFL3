package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class GetGlobalObjectEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public GetGlobalObjectEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		String name = (String) expression.getElement(0).evaluate(rti).getValue();
		Data res;
		XflEngine engine = getEngine();
		if (engine.hasGlobalObject(name)) {
			res = engine.getGlobalObjectWrapper(name);
		} else {
			res = new Data(expression, rti);
			res.setType(DataType.UNAVAILABLE);
		}
		return res;
	}

}