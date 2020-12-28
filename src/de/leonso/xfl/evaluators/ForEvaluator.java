package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

/**
 * @For( initialize ; condition ; increment ; statement ; ... )
 * 
 * @author Bert
 *
 */
public class ForEvaluator extends BlockEvaluator {
	private static final long serialVersionUID = 1L;

	public ForEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	protected Data processBlock(Expression expression, ArrayList<Expression> elements, Data defaultData, int iStart, Context context) throws Exception {

		Data dTemp = defaultData;
		Data res = defaultData;

		boolean cond;
		if (iStart == 0) {
			// initialize
			res = elements.get(0).evaluate(context);
			// defaultData.addChild(res);
			// condition
			dTemp = elements.get(1).evaluate(context);
			// defaultData.addChild(dTemp);
			cond = dTemp.isTrue();
		} else {
			cond = true;
		}
		while (cond) {

			for (int i = iStart > 0 ? iStart : 3; i < elements.size(); i++) {
				try {
					dTemp = evalAndCheckGoto(expression, i, elements, defaultData, context);
				} catch (InstructionPointerChangedException e) {
					i = e.getNewInstructionPointer(); // instruction pointer
														// umsetzen
				}
			}

			// wenn Einsprung via @Gosub, dann wieder von vorn
			if (iStart > 0) {
				iStart = 0;
				cond = false;
			}
			// increment
			res = elements.get(2).evaluate(context);
			// defaultData.addChild(res);
			// TODO check: convertToVarItem

			// condition
			dTemp = elements.get(1).evaluate(context);
			// defaultData.addChild(dTemp);
			cond = dTemp.isTrue();

			// aufräumen
			getEngine().removeUnusedItems();
		}

		return res;

	}

}
