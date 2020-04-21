package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class ParenthesesEvaluator extends BlockEvaluator {
	private static final long serialVersionUID = 1L;

	public ParenthesesEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data res = super.evaluate(expression, rti);
		switch (res.getType()) {
		case CODE_BOTH:
		case CODE_REF:
		case CODE_VAR:
			res.setText("(" + res.getText() + ")");
		default:
			break;
		}
		return res;
	}

}
