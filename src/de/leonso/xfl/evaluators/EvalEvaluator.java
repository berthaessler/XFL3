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
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data res = expression.getElement(0).evaluate(rti);
		String code = (String) res.getValue();
		res = new Data(expression, rti);
		res.assignValue(getEngine().eval(code, rti.getRefDoc()));
		return res;
	}

}
