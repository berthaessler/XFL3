package de.leonso.xfl.evaluators;

import de.leonso.core.UtilsString;
import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

/**
 * Ersetzt alle Sonderzeichen durch "normale" Buchstaben
 * https://stackoverflow.com/questions/4122170/java-change-%c3%a1%c3%a9%c5%91%c5%b1%c3%ba-to-aeouu/4122207#4122207
 */
public class NormalizeEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public NormalizeEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data data = expression.getElement(0).evaluate(context);
		Object value = data.getValue();
		if (value instanceof String) {
			Data res = new Data(expression, context);
			String convert = UtilsString.normalize((String) value);
			res.assignValue(convert);
			return res;
		}
		throw new IllegalArgumentException("@Normalize: String expected, " + value.getClass().getName() + " found");
	}

}
