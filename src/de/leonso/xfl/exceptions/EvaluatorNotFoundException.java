package de.leonso.xfl.exceptions;

import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.evaluators.AlphanumEvaluator;

/**
 * wird geworfen, wenn kein Evaluator fuer einen Ausdruck ermittelt werden konnte
 * 
 * @see XflEngine#getEvaluator(String, int)
 * @see AlphanumEvaluator#evaluate(Expression, Context)
 * 
 * @author Bert
 */
public class EvaluatorNotFoundException extends EvaluationException {
	private static final long serialVersionUID = 1L;

	private final String functionName;

	public EvaluatorNotFoundException(XflEngine engine, Expression exp) {
		super(engine, exp);
		this.functionName = exp.getTitle();
	}

	public EvaluatorNotFoundException(XflEngine engine, String functionName) {
		super(engine, null);
		this.functionName = functionName;
	}

	@Override
	public String getMessage() {
		return getEngine().getMessage("evaluator.not.found.for", functionName);
	}
}
