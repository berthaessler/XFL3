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
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data res = new Data(expression, context);
		res.setType(DataType.CODE_BOTH);
		if (expression.getElement(0).evaluate(context).getType() == DataType.UNAVAILABLE) {
			res.setText("@False");
		} else {
			res.setText("@True");
		}
		return res;
	}

}
