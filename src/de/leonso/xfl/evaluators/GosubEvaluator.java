package de.leonso.xfl.evaluators;

import java.util.Map;
import java.util.Map.Entry;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.LabelNotDefinedException;
import de.leonso.xfl.exceptions.ReturnException;

public class GosubEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public GosubEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		String title = expression.getElement(0).getTitle();
		// Kopie des Baumes anlegen
		Expression xTemp = expression.getParent();
		boolean found = false;
		while (!found) {
			// suchen wir die Sprungmarke in allen uebergeordneten Knoten
			if (xTemp.hasLabel(title)) {
				found = true;
			} else {
				xTemp = xTemp.getParent();
				if (xTemp == null) {
					throw new LabelNotDefinedException(getEngine(), expression, title);
				}
			}
		}
		Expression tmpRoot = xTemp; // TODO das muss auch ohne Kopie gehen
		Context tmpRti = expression.getEngine().createContext(tmpRoot, rti.getRefDoc());

		try {
			// Variablen uebergeben
			Map<String, Data> varsMap = rti.getVarsMap();
			for (Entry<String, Data> e : varsMap.entrySet()) {
				tmpRti.setVar(e.getKey(), e.getValue());
			}
			// Objekte uebergeben
			Map<String, Data> oMap = rti.getObjectsMap();
			for (Entry<String, Data> e : oMap.entrySet()) {
				tmpRti.setVar(e.getKey(), e.getValue());
			}

			getEngine().setSearchLabel(title);
			// falls das hier kein Wurzelknoten war, ist die Kopie als
			// Unterknoten
			// einer
			// Hilfswurzel angelegt worden. In diesem Fall nur den Unterknoten
			// ausfuehren
			Data tmpRes;
			try {
				if (tmpRoot.getElements().size() > 1) {
					tmpRes = tmpRoot.evaluate(tmpRti);
				} else {
					tmpRes = tmpRoot.getElement(0).evaluate(tmpRti);
				}
			} catch (ReturnException e) {
				tmpRes = e.getData();
			} finally {
				// Variablen uebergeben
				varsMap = tmpRti.getVarsMap();
				for (Entry<String, Data> e : varsMap.entrySet()) {
					Data data = e.getValue();
					Data copy = new Data(expression, rti);
					copy.assignItem(data.getItem()); // umspeichern
					rti.setVar(e.getKey(), copy);
				}
				// Objekte uebergeben
				oMap = tmpRti.getObjectsMap();
				for (Entry<String, Data> e : oMap.entrySet()) {
					rti.setVar(e.getKey(), e.getValue());
				}
			}

			Data res = new Data(expression, rti);
			res.assignItem(tmpRes.getItem()); // umspeichern in aktuelle RTI
			return res;
		} finally {
			tmpRti.close();
		}

	}

}
