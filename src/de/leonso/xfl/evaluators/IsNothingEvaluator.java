package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class IsNothingEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public IsNothingEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data res = new Data(expression, context);
		res.setType(DataType.CODE_BOTH);
		boolean isnothing;
		Data o = expression.getElement(0).evaluate(context);
		if (o.getType() == DataType.OBJECT) {
			if (o.getObject() == null) {
				isnothing = true;
			} else {
				isnothing = false;
			}
		} else if (o.getType() == DataType.UNAVAILABLE) {
			isnothing = true;
		} else {
			isnothing = false;
		}
		res.setText(isnothing ? "@True" : "@False");
		return res;
	}

}
