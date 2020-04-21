package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class IsAvailableEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public IsAvailableEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data res = new Data(expression, rti);
		res.setType(DataType.CODE_BOTH);
		if (expression.getElement(0).evaluate(rti).getType() == DataType.UNAVAILABLE) {
			res.setText("@False");
		} else {
			res.setText("@True");
		}
		return res;
	}

}
