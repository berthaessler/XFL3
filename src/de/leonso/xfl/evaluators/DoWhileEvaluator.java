package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

/**
 * @DoWhile( statement ; ... ; condition )
 * 
 * @author Bert
 *
 */
public class DoWhileEvaluator extends BlockEvaluator {
	private static final long serialVersionUID = 1L;

	public DoWhileEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	protected Data processBlock(Expression expression, ArrayList<Expression> elements, Data defaultData, int iStart, Context context) throws Exception {

		Data dTemp = null;
		Data res = defaultData;

		boolean cond;
		do {

			for (int i = 0; i < elements.size() - 1; i++) {
				try {
					res = evalAndCheckGoto(expression, i, elements, defaultData, context);
				} catch (InstructionPointerChangedException e) {
					i = e.getNewInstructionPointer(); // instruction pointer umsetzen
				}
			}

			// condition
			dTemp = elements.get(elements.size() - 1).evaluate(context);
			cond = dTemp.isTrue();

			// aufräumen
			getEngine().removeUnusedItems();

		} while (cond);

		return res;

	}

}
