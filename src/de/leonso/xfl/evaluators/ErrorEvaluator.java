package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.ErrorException;

public class ErrorEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public ErrorEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Integer errNum = ((Number) expression.getElement(0).evaluate(context).getValue()).intValue();
		String errMsg = null;
		if (expression.getElements().size() > 1) {
			errMsg = (String) expression.getElement(1).evaluate(context).getValue();
		}
		throw new ErrorException(getEngine(), expression, errNum, errMsg);
	}

}
