package de.leonso.xfl.evaluators;

import org.apache.commons.codec.binary.Hex;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

public class HexEncodeEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public HexEncodeEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data data = expression.getElement(0).evaluate(context);
		Object value = data.getValue();
		if (value instanceof String) {
			Data res = new Data(expression, context);
			String raw = (String) value;
			String hex = Hex.encodeHexString(raw.getBytes());
			res.assignValue(hex);
			return res;
		}
		throw new IllegalArgumentException("@HexEncode: String expected, " + value.getClass().getName() + " found");
	}

}
