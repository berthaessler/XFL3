package de.leonso.xfl.evaluators;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.EvaluationException;
import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class LotusScriptEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	Agent agent = null;
	Document transportDoc = null;
	String noteID = null;

	public LotusScriptEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public void close() throws Exception {
		if (agent != null) {
			boolean tmp = runWithDocumentContext;
			runWithDocumentContext = false; // Cannot remove NotesDocument when
											// it is the Document Context
			getTransportDoc().replaceItemValue("$Action", "Remove");
			runAgent();
			agent.recycle();
			agent = null;
			runWithDocumentContext = tmp; // wieder zurueck
		}
		if (transportDoc != null) {
			transportDoc.recycle();
			transportDoc = null;
		}
		noteID = null;
		super.close();
	}

	private Agent getAgent() throws Exception {
		if (agent == null) {
			Database db = getEngine().getCurrentDatabase();
			agent = db.getAgent("(XFLOnServer)");
			if (agent == null) {
				// TODO
				throw new Exception("agent '(XFLOnServer)' not found in " + db.getServer() + "!!" + db.getFilePath());
			}
		}
		return agent;
	}

	private Document getTransportDoc() throws Exception {
		if (transportDoc == null) {
			Session session = getEngine().getSession();
			Database db = getEngine().getCurrentDatabase();

			if (noteID == null) {
				transportDoc = db.getProfileDocument("XFLOnServerExecuter", session.getUserName());
				noteID = transportDoc.getNoteID();
			} else {
				transportDoc = db.getDocumentByID(noteID);
			}
		}
		return transportDoc;
	}

	public void scanLibs() throws Exception {
		getTransportDoc().replaceItemValue("$Action", "getUDF");
		runAgent();
		Vector<?> res = getTransportDoc().getItemValue("$udf");
		for (int i = 0; i < res.size(); i++) {
			String n = (String) res.get(i);
			getEngine().addEvaluator(n, this);
		}
	}

	private boolean runWithDocumentContext = true;

	private void runAgent() throws Exception {
		if (runWithDocumentContext) {
			try {
				getAgent().runWithDocumentContext(getTransportDoc());
			} catch (NotesException e) {
				// Unable to pass doc context - Caller must run with user
				// authority
				runWithDocumentContext = false;
				// Agent erstmal wegwerfen, sonst geht agent.run(id) nicht
				// NotesException: Agents cannot run recursively
				// warum auch immer...
				agent.recycle();
				agent = null;
			} catch (Exception e) {
				throw e;
			}
		}
		if (!runWithDocumentContext) {
			Document doc = getTransportDoc();
			doc.save(); // das ist bei runWithDocumentContext nicht noetig
			getAgent().run(doc.getNoteID());
			reloadDoc();
		}
	}

	private Document reloadDoc() throws Exception {
		transportDoc.recycle();
		transportDoc = null;
		return getTransportDoc();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		// jeder Parameter wird in eine Globale Variable konvertiert

		Document pdoc = getTransportDoc();

		// Felder zuruecksetzen
		boolean cleaned = false;
		Vector<Item> items = pdoc.getItems();
		for (Item item : items) {
			String itemName = item.getName();
			if ("$name".equalsIgnoreCase(itemName)) {
				// behalten
			} else if ("$revisions".equalsIgnoreCase(itemName)) {
				// behalten
			} else if ("$updatedby".equalsIgnoreCase(itemName)) {
				// behalten
			} else if ("$conflictaction".equalsIgnoreCase(itemName)) {
				// behalten
			} else if ("$nopurge".equalsIgnoreCase(itemName)) {
				// behalten
			} else {
				// alle Spuren loeschen
				item.remove();
				cleaned = true;
			}
			item.recycle();
		}
		// wenn das Dokument noch "verschmutzt" war, erst einmal speichern
		if (cleaned) {
			pdoc.save();
		}

		Data res = new Data(expression, rti);
		res.setType(DataType.UNAVAILABLE);

		// alle globalen Variablen durchreichen
		Map<String, Data> all = getEngine().getAllGlobalVars();
		for (java.util.Map.Entry<String, Data> e : all.entrySet()) {
			String name = e.getKey();
			Data data = e.getValue();
			Item item = data.convertToVarItem().getItem();
			Object value = item.getValues();
			pdoc.replaceItemValue("$global_" + name, value);
		}

		String code = expression.getTitle();

		// alle Parameter ebenfalls auf globale Variablen
		ArrayList<Expression> elements = expression.getElements();
		if (elements.size() > 0) {
			code += "(";
			for (int i = 0; i < elements.size(); i++) {
				Expression ex = elements.get(i);
				Data data = ex.evaluate(rti);
				String varName = "J2LS_" + i;
				code += (i > 0 ? "; " : "") + "GLOBAL " + varName;
				pdoc.replaceItemValue("$global_" + varName, data.getValue());
			}
			code += ")";
		}

		// Dokument-Referenz mitgeben
		Document refDoc = rti.getRefDoc();
		if (refDoc != null) {
			String nid = refDoc.getNoteID();
			if (!"0".equals(nid)) {
				pdoc.replaceItemValue("$RefID", nid);
			}
		}

		// Code per LS-Agent ausfuehren
		pdoc.replaceItemValue("$Action", "Execute");
		pdoc.replaceItemValue("$Code", code);
		runAgent();
		pdoc = getTransportDoc();

		String err = pdoc.getItemValueString("$Error");
		if (!"".equals(err)) {
			throw new EvaluationException(getEngine(), expression, err);
		}
		Vector<?> ret = pdoc.getItemValue("$Return");

		// alle globalen Variablen zurueckholen
		items = pdoc.getItems();
		for (Item item : items) {
			String itemName = item.getName().toLowerCase();
			if (itemName.startsWith("$global_")) {
				String varName = strRight(itemName, "$global_");
				Vector<?> values = item.getValues();
				if (values == null) {
					getEngine().removeGlobalVar(varName);
				} else if (values.size() == 1) {
					getEngine().setGlobalVar(varName, values.get(0));
				} else {
					getEngine().setGlobalVar(varName, values);
				}
				item.remove();
			} else if ("$name".equalsIgnoreCase(itemName)) {
				// behalten
			} else if ("$revisions".equalsIgnoreCase(itemName)) {
				// behalten
			} else if ("$updatedby".equalsIgnoreCase(itemName)) {
				// behalten
			} else if ("$conflictaction".equalsIgnoreCase(itemName)) {
				// behalten
			} else if ("$nopurge".equalsIgnoreCase(itemName)) {
				// behalten
			} else {
				// alle Spuren loeschen
				item.remove();
			}
			item.recycle();
		}

		res.assignValue(ret);
		return res;
	}

	private static String strRight(String input, String delimiter) {
		if (input == null) {
			return "";
		}
		return input.indexOf(delimiter) == -1 ? "" : input.substring(input.indexOf(delimiter) + delimiter.length());
	}

}
