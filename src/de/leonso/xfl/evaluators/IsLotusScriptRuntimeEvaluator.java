package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class IsLotusScriptRuntimeEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public IsLotusScriptRuntimeEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data res = new Data(expression, rti);
		res.setType(DataType.CODE_BOTH);
		res.setText("@False");
		return res;
	}

}