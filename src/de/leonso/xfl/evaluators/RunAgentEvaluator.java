package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import lotus.domino.Agent;
import lotus.domino.Database;

public class RunAgentEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public RunAgentEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data data = expression.getElement(0).evaluate(rti);
		String agentName = (String) data.getValue();
		Data res = new Data(expression, rti);
		res.setType(DataType.CODE_BOTH);
		res.setText("{}");
		Database db = getEngine().getCurrentDatabase();
		Agent agent = db.getAgent(agentName);
		if (agent == null) {
			throw new RuntimeException("agent '" + agentName + "' not found");
		}
		agent.run();
		return res;
	}

}
