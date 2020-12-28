package de.leonso.xfl.evaluators;

import de.leonso.core.notes.api.DocumentWrapper;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class SetRefDocEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public SetRefDocEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data data = expression.getElement(0).evaluate(context);
		Object doc = data.getObject();
		if (!(doc instanceof DocumentWrapper)) {
			throw new IllegalArgumentException("Es muss ein Objekt der Klasse Document übergeben werden");
		}
		context.setRefDoc((DocumentWrapper) doc);
		return data;
	}

}
