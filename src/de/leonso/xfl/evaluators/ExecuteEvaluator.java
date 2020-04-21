package de.leonso.xfl.evaluators;

import java.util.List;

import de.leonso.core.LIModel;
import de.leonso.core.LIObject;
import de.leonso.core.exception.LDatabaseAccessException;
import de.leonso.core.exception.LDeleteException;
import de.leonso.core.exception.LException;
import de.leonso.core.exception.LLoadException;
import de.leonso.core.notes.LApplicationNotes;
import de.leonso.core.notes.LIApplicationNotes;
import de.leonso.core.notes.LINativeNotesSessionProvider;
import de.leonso.core.notes.LLotusScriptEvaluator;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class ExecuteEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	private LIApplicationNotes app = null;

	public ExecuteEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Object test = expression.getElement(0).evaluate(rti).getValue();
		String code;
		if (test instanceof String) {
			code = (String) test;
		} else {
			throw new IllegalArgumentException("Wert für Parameter 'code' ungültig, Text erwartet. (" + test + ")");
		}
		if (app == null) {
			app = (LIApplicationNotes) getEngine().getGlobalObject("Application");
		}
		if (app == null) {

			LINativeNotesSessionProvider sessionProvider = getEngine().getNotesFactoryProvider().getNotesFactory().getSessionProvider();

			app = new LApplicationNotes(sessionProvider) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean deleteInstance(Object object) throws LDeleteException {
					return false;
				}

				@Override
				public LIObject instantiate(String classtype) {
					return null;
				}

				@Override
				public Object getInstanceFromCacheOf(String classtype, String keytype, String keyvalue) throws LException {
					return null;
				}

				@Override
				public Object createInstanceOf(String classtype) {
					return null;
				}

				@Override
				public LIModel getModelBy(String classtype, String keytype, String keyvalue) throws LLoadException {
					return null;
				}

				@Override
				public List<LIModel> getAllModelsBy(String classtype, String keytype, String keyvalue) throws LLoadException {
					return null;
				}

				@Override
				protected void initializePools() throws LDatabaseAccessException {
				}

				@Override
				protected List<String> getPrivilegeBundleNotes() {
					return null;
				}

				@Override
				protected List<String> getPrivilegeBundleDefinition() {
					return null;
				}
			};
		}

		LLotusScriptEvaluator evaluator = new LLotusScriptEvaluator(app);
		String lsResult = (String) evaluator.evaluate(code);
		Data data = new Data(expression, rti);
		data.assignValue(lsResult);
		return data;
	}

}
