package de.leonso.xfl.evaluators;

import org.apache.commons.codec.binary.Base64;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

public class Base64EncodeEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public Base64EncodeEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data data = expression.getElement(0).evaluate(context);
		Object value = data.getValue();
		if (value instanceof String) {
			Data res = new Data(expression, context);
			String raw = (String) value;
			byte[] base64 = Base64.encodeBase64(raw.getBytes(), false); // ohne Umbruch
			String convert = new String(base64);
			res.assignValue(convert);
			return res;
		}
		throw new IllegalArgumentException("@Base64Decode: String expected, " + value.getClass().getName() + " found");
	}

}
