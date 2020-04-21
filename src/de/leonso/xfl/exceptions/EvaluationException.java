package de.leonso.xfl.exceptions;

import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

public class EvaluationException extends XflException {
	private static final long serialVersionUID = 1L;

	private final Expression exp;

	public EvaluationException(XflEngine engine, Expression exp, String msg) {
		super(engine, msg);
		this.exp = exp;
	}

	public EvaluationException(XflEngine engine, Expression exp, Throwable cause) {
		super(engine, cause);
		this.exp = exp;
	}

	public EvaluationException(XflEngine engine, Expression exp) {
		super(engine, "");
		this.exp = exp;
	}

	public Expression getExpression() {
		return exp;
	}

	/**
	 * ursaechliche Exception
	 * 
	 * @return
	 */
	public Throwable getRootCause() {
		Throwable e = this;
		Throwable cause = e.getCause();
		while (cause != null) {
			e = cause;
			cause = e.getCause();
		}
		return e;
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		return getEngine().getMessage("exception.in.expression", msg, exp);
	}
}
