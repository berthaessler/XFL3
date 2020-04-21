package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.ReturnException;

public class ReturnEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public ReturnEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data res = expression.getElement(0).evaluate(rti);
		throw new ReturnException(getEngine(), expression, res);
	}

}