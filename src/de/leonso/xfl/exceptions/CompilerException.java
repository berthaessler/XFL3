package de.leonso.xfl.exceptions;

import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

public class CompilerException extends XflException {
	private static final long serialVersionUID = 1L;

	private Expression expression;

	public CompilerException(XflEngine engine, String msg, Expression expression) {
		super(engine, msg);
		this.expression = expression;
	}

	public CompilerException(XflEngine engine, Expression expression, Throwable cause) {
		super(engine, cause);
		this.expression = expression;
	}

	@Override
	public String getMessage() {
		Integer startPos = expression.getStartPos();
		String code = expression.getRoot().getCode();
		// ein paar Zeichen dazu ausgeben
		Integer codeOutputBeginPos = startPos - 100;
		String pre;
		if (codeOutputBeginPos < 0) {
			codeOutputBeginPos = 0;
			pre = "";
		} else {
			pre = "...";
		}
		String output = code.substring(codeOutputBeginPos, startPos);
		return getEngine().getMessage(getRawMessage(), pre + output + "<-- (Position " + startPos + ")");
	}

}
