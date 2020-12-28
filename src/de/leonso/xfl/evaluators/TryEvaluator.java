package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.core.UtilsString;
import de.leonso.core.exception.LException;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

/**
 * @Try( statement [; errorVariableName [; errorStatement [; finallyStatement]]])
 * 
 * @author Bert
 *
 */
public class TryEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public TryEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data res = new Data(expression, context);
		ArrayList<Expression> elements = expression.getElements();
		Expression statement = elements.get(0);

		try {
			res.assignValue(statement.evaluate(context).getValue());
		} catch (Throwable e) {

			// Fehler als Variable bereitstellen.
			if (elements.size() > 1) {
				Data errorVar = elements.get(1).evaluate(context);
				String varName = (String) errorVar.getValue();
				Data error = new Data(expression, context);
				String message = LException.getRootMessage(e); // e.getMessage();
				error.assignValue(UtilsString.isNullOrEmpty(message) ? e.getClass().getName() : message);
				res.addChild(error);
				context.setObject(varName, error);

				// Statement
				if (elements.size() > 2) {
					Expression statementError = elements.get(2);
					res.assignValue(statementError.evaluate(context).getValue());
				} else {
					res.assignValue(message);
				}
			} else {
				// kein catch-Zweig
				res.assignValue(0); // false
			}

		} finally {

			// optionales Finally-Statement
			if (elements.size() > 3) {
				Expression statementFinal = elements.get(3);
				res.assignValue(statementFinal.evaluate(context).getValue());
			}
		}

		return res;

	}

}
