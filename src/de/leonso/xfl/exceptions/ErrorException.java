package de.leonso.xfl.exceptions;

import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

public class ErrorException extends EvaluationException {
	private static final long serialVersionUID = 1L;

	private final Integer errNum;
	public ErrorException(XflEngine engine, Expression exp, Integer errNum, String msg) {
		super(engine, exp, msg);
		this.errNum = errNum;
	}
	
	public Integer getErrNum() {
		return errNum;
	}
	
	@Override
	public String getMessage() {
		// nicht uebersetzen
		return super.getRawMessage();
	}
}
