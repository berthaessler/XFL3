package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.GotoException;

public class GotoEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public GotoEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {

		// aufräumen
		getEngine().removeUnusedItems();

		throw new GotoException(getEngine(), expression, expression.getElement(0).getTitle());
	}

}
