package de.leonso.xfl.evaluators;

import java.util.ArrayList;
import java.util.List;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.XflExtension;

public class ExtensionEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	final XflExtension ext;

	public ExtensionEvaluator(XflEngine engine, XflExtension ext) {
		super(engine);
		this.ext = ext;
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		ext.setExpression(expression);

		Data res = new Data(expression, context);

		String title = expression.getTitle();

		List<Object> argsList = new ArrayList<Object>();
		ArrayList<Expression> elements = expression.getElements();
		try {
			for (Expression exp : elements) {
				Object value = exp.evaluate(context).getValue();
				argsList.add(value);
			}
			Object[] args = argsList.toArray();
			Object ret = ext.evaluate(context, title, args);
			res.assignValue(ret);
			return res;

		} catch (Exception e) {
			// zur Fehlersuche
			throw e;
		}
	}

}
