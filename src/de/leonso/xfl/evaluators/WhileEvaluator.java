package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

/**
 * @While( condition ; statement ; ... )
 * 
 * @author Bert
 *
 */
public class WhileEvaluator extends BlockEvaluator {
	private static final long serialVersionUID = 1L;

	public WhileEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	protected Data processBlock(Expression expression, ArrayList<Expression> elements, Data defaultData, int iStart, Context context) throws Exception {

		Data dTemp = null;
		Data res = defaultData;

		// condition
		dTemp = elements.get(0).evaluate(context);
		defaultData.addChild(dTemp);
		boolean cond = dTemp.isTrue();

		while (cond) {

			for (int i = 1; i < elements.size(); i++) {
				try {
					res = evalAndCheckGoto(expression, i, elements, defaultData, context);
				} catch (InstructionPointerChangedException e) {
					i = e.getNewInstructionPointer(); // instruction pointer
														// umsetzen
				}
			}

			// condition
			dTemp = elements.get(0).evaluate(context);
			cond = dTemp.isTrue();

			// aufräumen
			getEngine().removeUnusedItems();
		}

		return res;

	}

}
