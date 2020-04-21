package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class IfEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public IfEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {

		ArrayList<Expression> elements = expression.getElements();
		Data res = new Data(expression, rti);
		res.setType(DataType.UNAVAILABLE);
		Data dTemp = null;

		int i = 0;

		while ((i + 1 < elements.size())) {
			try {
				// Bedingung prüfen
				dTemp = elements.get(i).evaluate(rti);
			} catch (Exception e) {
				throw e;
			}
			// res.addChild(dTemp);
			if (dTemp.isTrue()) {
				dTemp = elements.get(i + 1).evaluate(rti);
				res.addChild(dTemp);
				dTemp.convertToVarItem();
				return dTemp;
			} else {
				i += 2;
			}
		}

		// letzter ELSE-Zweig (könnte auch fehlen)
		if (i < elements.size()) {
			dTemp = elements.get(i).evaluate(rti);
			res.addChild(dTemp);
			dTemp.convertToVarItem();
		} else {
			// wenn kein ELSE angegeben ist, liefern wir @False (= 0) zurück
		}

		return dTemp;
	}

}
