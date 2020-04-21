package de.leonso.xfl.exceptions;

import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

public class NotesFormulaEvaluationException extends EvaluationException {
	private static final long serialVersionUID = 1L;

	public NotesFormulaEvaluationException(XflEngine engine, Expression exp, Throwable e) {
		super(engine, exp, e);
	}

	public NotesFormulaEvaluationException(XflEngine engine, Expression exp) {
		super(engine, exp);
	}

	public NotesFormulaEvaluationException(XflEngine engine, Expression exp, Exception cause) {
		super(engine, exp, cause);
	}

	@Override
	public String getMessage() {
		return getEngine().getMessage("could.not.evaluate.formula", getExpression().getCode());
	}

}
