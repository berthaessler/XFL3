package de.leonso.xfl;

public abstract class XflExtension implements IXflExtension {
	private static final long serialVersionUID = 1L;

	private Expression expression = null;

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

}
