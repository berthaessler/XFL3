package de.leonso.xfl.evaluators;

import java.util.Vector;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

/**
 * @Transform( list ; variableName ; formula )
 * 
 * @author Bert
 *
 */
public class TransformEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public TransformEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data res = new Data(expression, context);
		Data listData = expression.getElement(0).evaluate(context);
		res.addChild(listData);
		Data varData = expression.getElement(1).evaluate(context);
		res.addChild(varData);

		Object vlist = listData.getValue();
		Object[] list = null;
		if (vlist instanceof Object[]) {
			list = (Object[]) vlist;
		} else {
			list = new Object[1];
			list[0] = vlist;
		}

		Vector<Object> resList = new Vector<Object>();
		String var = (String) varData.getValue();
		for (int i = 0; i < list.length; i++) {
			Object value = list[i];
			res.assignValue(value);
			context.setVar(var, res); // Laufvariable setzen

			// Formel
			Data ev = expression.getElement(2).evaluate(context);
			Object value2 = ev.getValue();

			// alle Ergebnisse durchlaufen
			if (value2 instanceof Object[]) {
				Object[] l2 = (Object[]) value2;
				for (int j = 0; j < l2.length; j++) {
					Object object = l2[j];
					resList.add(object);
				}
			} else {
				resList.add(value2);
			}
		}
		res = new Data(expression, context);
		res.assignValue(resList);
		return res;

	}

}
