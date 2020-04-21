package de.leonso.xfl.evaluators;

import org.apache.commons.codec.binary.Base64;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class Base64DecodeEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public Base64DecodeEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data data = expression.getElement(0).evaluate(rti);
		Object value = data.getValue();
		if (value instanceof String) {
			Data res = new Data(expression, rti);
			byte[] decodeBase64 = Base64.decodeBase64((String) value);
			String convert = new String(decodeBase64);
			res.assignValue(convert);
			return res;
		}
		throw new IllegalArgumentException("@Base64Decode: String expected, " + value.getClass().getName() + " found");
	}

}
