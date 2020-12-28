package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

/**
 * erzeugt ein Objekt einer Java-Klasse<br>
 * z.B: @CreateJavaObject("java.lang.String")
 */
public class CreateJavaObjectEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public CreateJavaObjectEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data data = expression.getElement(0).evaluate(context);
		Object value = data.getValue();
		if (value instanceof String) {
			Data res = new Data(expression, context);
			String className = (String) value;
			Class<?> cl = Class.forName(className);
			Object inst = cl.newInstance();
			res.assignObject(inst);
			return res;
		}
		throw new IllegalArgumentException("@CreateJavaObject: String expected, " + value.getClass().getName() + " found");
	}

}
