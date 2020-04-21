package de.leonso.xfl.exceptions;

import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.evaluators.AssignmentEvaluator;
import de.leonso.xfl.evaluators.IndexEvaluator;

/**
 * 
 * @see AssignmentEvaluator#evaluate(Expression, Context)
 * @see IndexEvaluator#evaluate(Expression, Context)
 * 
 * @author Bert
 *
 */
public class SubscriptOutOfRangeException extends EvaluationException {
	private static final long serialVersionUID = 1L;

	private final int index;

	public SubscriptOutOfRangeException(XflEngine engine, Expression exp, int index) {
		super(engine, exp);
		this.index = index;
	}

	@Override
	public String getMessage() {
		return getEngine().getMessage("subscript.out.of.range", index, getExpression());
	}
}
