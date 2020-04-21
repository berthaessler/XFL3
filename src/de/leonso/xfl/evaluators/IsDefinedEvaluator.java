package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class IsDefinedEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public IsDefinedEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		String name = (String) expression.getElement(0).evaluate(rti).getValue();
		int paramCount = (Integer) expression.getElement(2).evaluate(rti).getValue();
		Data res = new Data(expression, rti);
		res.setType(DataType.CODE_BOTH);
		if (getEngine().isUDFDefined(name, paramCount)) {
			res.setText("@True");
		} else {
			res.setText("@False");
		}
		return res;
	}

}
