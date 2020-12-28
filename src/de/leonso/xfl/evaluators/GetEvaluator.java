package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class GetEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public GetEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		String name = (String) expression.getElement(0).evaluate(context).getValue();
		Data res;
		if (context.hasVar(name)) {
			res = context.getVar(name);
		} else {
			res = new Data(expression, context);
			res.setType(DataType.UNAVAILABLE);
		}
		return res;
	}

}
