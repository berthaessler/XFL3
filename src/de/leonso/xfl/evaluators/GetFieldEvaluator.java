package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import lotus.domino.Document;
import lotus.domino.Item;

public class GetFieldEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public GetFieldEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {

		String name = (String) expression.getElement(0).evaluate(context).getValue();
		Document refDoc = context.getRefDoc();

		Data res = new Data(expression, context);
		if (refDoc.hasItem(name)) {
			Item item = refDoc.getFirstItem(name);
			res.assignItem(item);
		} else {
			res.setType(DataType.UNAVAILABLE);
		}

		return res;

	}

}
