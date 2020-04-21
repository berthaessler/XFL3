package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import lotus.domino.Document;

public class SetFieldEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public SetFieldEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {

		String var = (String) expression.getElement(0).evaluate(rti).getValue();
		Document refDoc = rti.getRefDoc();

		Data res = expression.getElement(1).evaluate(rti);
		if (res.getType() == DataType.UNAVAILABLE) {
			refDoc.removeItem(var);
		} else {
			refDoc.removeItem(var);
			// dabei werden Flags wie AUTHORS geloescht !!! Anders als in
			// normaler Formelsprache
			res.getItem().copyItemToDocument(refDoc, var);
		}

		return res;
	}

}
