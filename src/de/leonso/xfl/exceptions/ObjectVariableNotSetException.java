package de.leonso.xfl.exceptions;

import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

public class ObjectVariableNotSetException extends EvaluationException {
	private static final long serialVersionUID = 1L;

	public ObjectVariableNotSetException(XflEngine engine, Expression exp) {
		super(engine, exp);
	}

	@Override
	public String getMessage() {
		return getEngine().getMessage("object.variable.not.set", getExpression().getElement(0).getTitle(), getExpression().getCode());
	}
	
}
