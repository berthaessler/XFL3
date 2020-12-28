package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class NothingEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public NothingEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data res = new Data(expression, context);
		res.assignObject(null);
		return res;
	}

}
