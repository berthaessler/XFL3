package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import lotus.domino.Base;

public class RecycleEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public RecycleEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data res = new Data(expression, rti);
		res.setType(DataType.CODE_BOTH);
		res.setText("0");

		ArrayList<Expression> elements = expression.getElements();
		for (Expression exp : elements) {
			Data data = exp.evaluate(rti);
			Object object = data.getObject();
			if (object == null) {
				// OK
			} else if (object instanceof Base) {
				Base b = (Base) object;
				b.recycle();
			} else {
				throw new RuntimeException("Object " + object + " is not recyclable");
			}
		}

		return res;
	}

}
