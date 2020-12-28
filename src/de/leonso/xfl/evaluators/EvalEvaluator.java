package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class EvalEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public EvalEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data res = expression.getElement(0).evaluate(context);
		String code = (String) res.getValue();
		res = new Data(expression, context);
		res.assignValue(getEngine().eval(code, context.getRefDoc()));
		return res;
	}

}
