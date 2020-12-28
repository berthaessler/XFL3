package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.EvaluationException;

/**
 * benutzt f�r konstante Ausdruecke, z.B. [01.01.2099]
 * 
 * @author Bert
 *
 */
public class ConstantEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public ConstantEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws EvaluationException {
		Data res = new Data(expression, context);
		res.assignCodeConstant(DataType.CODE_BOTH, expression.getTitle());
		return res;
	}

}
