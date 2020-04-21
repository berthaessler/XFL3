package de.leonso.xfl;

import java.io.Reader;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import de.leonso.core.exception.LDatabaseAccessException;
import de.leonso.core.exception.LException;
import de.leonso.core.notes.LINotesFactoryProvider;
import de.leonso.core.notes.api.NotesFactory;
import de.leonso.xfl.evaluators.AlphanumEvaluator;
import de.leonso.xfl.evaluators.AssignmentEvaluator;
import de.leonso.xfl.evaluators.Base64DecodeEvaluator;
import de.leonso.xfl.evaluators.Base64EncodeEvaluator;
import de.leonso.xfl.evaluators.BinaryOperatorEvaluator;
import de.leonso.xfl.evaluators.ConstantEvaluator;
import de.leonso.xfl.evaluators.CreateJavaObjectEvaluator;
import de.leonso.xfl.evaluators.DebugEvaluator;
import de.leonso.xfl.evaluators.DeleteDocumentEvaluator;
import de.leonso.xfl.evaluators.DeleteFieldEvaluator;
import de.leonso.xfl.evaluators.DoEvaluator;
import de.leonso.xfl.evaluators.DoWhileEvaluator;
import de.leonso.xfl.evaluators.DotEvaluator;
import de.leonso.xfl.evaluators.ErrorEvaluator;
import de.leonso.xfl.evaluators.EvalEvaluator;
import de.leonso.xfl.evaluators.Evaluator;
import de.leonso.xfl.evaluators.ExecuteEvaluator;
import de.leonso.xfl.evaluators.ExtensionEvaluator;
import de.leonso.xfl.evaluators.ForEvaluator;
import de.leonso.xfl.evaluators.GetDocFieldEvaluator;
import de.leonso.xfl.evaluators.GetEvaluator;
import de.leonso.xfl.evaluators.GetFieldEvaluator;
import de.leonso.xfl.evaluators.GetGlobalEvaluator;
import de.leonso.xfl.evaluators.GetGlobalObjectEvaluator;
import de.leonso.xfl.evaluators.GetObjectEvaluator;
import de.leonso.xfl.evaluators.GosubEvaluator;
import de.leonso.xfl.evaluators.GotoEvaluator;
import de.leonso.xfl.evaluators.HexEncodeEvaluator;
import de.leonso.xfl.evaluators.HtmlDecodeEvaluator;
import de.leonso.xfl.evaluators.HtmlEncodeEvaluator;
import de.leonso.xfl.evaluators.IfEvaluator;
import de.leonso.xfl.evaluators.IndexEvaluator;
import de.leonso.xfl.evaluators.IsAvailableEvaluator;
import de.leonso.xfl.evaluators.IsDefinedEvaluator;
import de.leonso.xfl.evaluators.IsJavaRuntimeEvaluator;
import de.leonso.xfl.evaluators.IsLotusScriptRuntimeEvaluator;
import de.leonso.xfl.evaluators.IsNewDocEvaluator;
import de.leonso.xfl.evaluators.IsNothingEvaluator;
import de.leonso.xfl.evaluators.IsUnAvailableEvaluator;
import de.leonso.xfl.evaluators.LabelEvaluator;
import de.leonso.xfl.evaluators.LogicalOperatorEvaluator;
import de.leonso.xfl.evaluators.LotusScriptEvaluator;
import de.leonso.xfl.evaluators.NewLineEvaluator;
import de.leonso.xfl.evaluators.NormalizeEvaluator;
import de.leonso.xfl.evaluators.NormalizeExceptUmlautEvaluator;
import de.leonso.xfl.evaluators.NotesFormulaEvaluator;
import de.leonso.xfl.evaluators.NothingEvaluator;
import de.leonso.xfl.evaluators.ParenthesesEvaluator;
import de.leonso.xfl.evaluators.PrintEvaluator;
import de.leonso.xfl.evaluators.PromptEvaluator;
import de.leonso.xfl.evaluators.RecycleEvaluator;
import de.leonso.xfl.evaluators.ReturnEvaluator;
import de.leonso.xfl.evaluators.RootEvaluator;
import de.leonso.xfl.evaluators.RunAgentEvaluator;
import de.leonso.xfl.evaluators.SaveDocumentEvaluator;
import de.leonso.xfl.evaluators.SetDocFieldEvaluator;
import de.leonso.xfl.evaluators.SetEvaluator;
import de.leonso.xfl.evaluators.SetFieldEvaluator;
import de.leonso.xfl.evaluators.SetGlobalEvaluator;
import de.leonso.xfl.evaluators.SetGlobalObjectEvaluator;
import de.leonso.xfl.evaluators.SetObjectEvaluator;
import de.leonso.xfl.evaluators.SetRefDocEvaluator;
import de.leonso.xfl.evaluators.TransformEvaluator;
import de.leonso.xfl.evaluators.TryEvaluator;
import de.leonso.xfl.evaluators.WhileEvaluator;
import de.leonso.xfl.evaluators.XflVersionEvaluator;
import de.leonso.xfl.exceptions.CompilerException;
import de.leonso.xfl.exceptions.EvaluationException;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class XflEngine implements ScriptEngine, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String VERSION = "3.29"; // für @XFLVersion

	private static final String EVALUATOR_ALPHANUM = "<alphanum>";

	private static final String EVALUATOR_SFL = "<default>";

	private static final String EVALUATOR_CONSTANT = "<number>";

	private static final String EVALUATOR_LABEL = "<label>";

	// Name properties-Datei mit den Meldungen
	private static final String RESOURCE_MESSAGES = "de.leonso.xfl.messages/messages";

	public static final int MAX_CODE_LENGTH = 2000; // 2048, aber wir brauchen
													// noch ein paar Zeichen
													// Reserve wegen FIELD xxx
													// := ...

	private XflEngineFactory factory;

	/**
	 * ueberschreibt ein Feld im Ref-Dokument. Die Flags sollen erhalten bleiben
	 * 
	 * @param doc
	 * @param fieldName
	 * @param source
	 * @throws NotesException
	 */
	public static void setFieldValue(Document doc, String fieldName, Item source) throws Exception {
		// ueberschreibt ein Feld im Ref-Dokument. Die Flags sollen erhalten
		// bleiben
		if (doc == null) {
			throw new Exception("doc.is.null");
		}

		boolean flags[] = new boolean[8];
		boolean itemExisted = false;
		Item it;

		it = doc.getFirstItem(fieldName);
		if (it != null) {
			flags[0] = it.isAuthors();
			flags[1] = it.isReaders();
			flags[2] = it.isSummary();
			flags[3] = it.isEncrypted();
			flags[4] = it.isNames();
			flags[5] = it.isProtected();
			flags[6] = it.isSigned();
			flags[7] = it.isSaveToDisk();
			itemExisted = true;
			doc.removeItem(fieldName); // dabei werden Flags wie AUTHORS
										// geloescht !!!
		}

		it = source.copyItemToDocument(doc, fieldName);

		if (itemExisted) {
			it.setAuthors(flags[0]);
			it.setReaders(flags[1]);
			it.setSummary(flags[2]);
			it.setEncrypted(flags[3]);
			it.setNames(flags[4]);
			it.setProtected(flags[5]);
			it.setSigned(flags[6]);
			it.setSaveToDisk(flags[7]);
		}

	}

	private final Map<String, String> aliases = new HashMap<String, String>();

	// user defined functions
	private final Map<String, Expression> udf = new HashMap<String, Expression>();

	// global objects
	private final Map<String, Data> globalObjects = new HashMap<String, Data>();
	private final Map<String, Object> objectStore = new HashMap<String, Object>();

	private final Map<String, Evaluator> defaultEvaluators = new HashMap<String, Evaluator>();
	private final Map<String, Evaluator> functionEvaluators = new HashMap<String, Evaluator>();

	private final Map<String, Evaluator> userDefinedEvaluators = new HashMap<String, Evaluator>();

	private final XflBindings bindings = new XflBindings(this);
	final LINotesFactoryProvider notesFactoryProvider;

	private transient Database currentDb;

	private XflEngine(XflEngineFactory fac, LINotesFactoryProvider notesFactoryProvider, Database currDb) {
		factory = fac == null ? new XflEngineFactory() : fac;
		this.notesFactoryProvider = notesFactoryProvider;
		this.currentDb = currDb;
	}

	public XflEngine(LINotesFactoryProvider notesFactoryProvider) {
		this(null, notesFactoryProvider);
	}

	public XflEngine(XflEngineFactory fac, LINotesFactoryProvider notesFactoryProvider) {
		this(fac, notesFactoryProvider, null);
	}

	public XflEngine(Database currDb) {
		this(null, currDb);
	}

	public XflEngine(XflEngineFactory fac, Database currDb) {
		this(fac, null, currDb);
	}

	public LINotesFactoryProvider getNotesFactoryProvider() {
		return notesFactoryProvider;
	}

	protected NotesFactory getNotesFactory() throws Exception {
		return notesFactoryProvider.getNotesFactory();
	}

	public Session getSession() throws Exception {
		return notesFactoryProvider != null ? notesFactoryProvider.getSession() : currentDb.getParent();
	}

	public Database getCurrentDatabase() throws LDatabaseAccessException {
		try {
			return getSession().getCurrentDatabase();
		} catch (Throwable e) {
			throw new LDatabaseAccessException(this, e);
		}
	}

	/**
	 * Haengt ein neues Token an die Liste.
	 * 
	 * @param elements
	 * @param word
	 * @param type
	 * @param tempPos
	 * @throws Exception
	 */
	private void addToken(List<Token> elements, String word, ParseType type, int tempPos) throws ScriptException {
		boolean up = true;
		int i = elements.size();
		Token last = i > 0 ? elements.get(i - 1) : null;

		if (type != ParseType.STRING) {
			if (type == ParseType.STRING_START) {
				throw new ScriptException("Missing end of quoted string: " + word + "<---");
			}
			if (i > 0) {
				if (elements.get(i - 1).getWord().equals("FIELD")) {
					up = false;
				}
			}

			if (up) {
				word = word.toUpperCase();
			}

		}

		Token e = new Token(last, word, type, tempPos, tempPos + word.length() - 1);
		elements.add(e);
	}

	public void close() throws Exception {
		bindings.clear();
		Collection<Evaluator> evaluators = defaultEvaluators.values();
		for (Evaluator evaluator : evaluators) {
			evaluator.close();
		}
		evaluators = functionEvaluators.values();
		for (Evaluator evaluator : evaluators) {
			evaluator.close();
		}
		evaluators = userDefinedEvaluators.values();
		for (Evaluator evaluator : evaluators) {
			evaluator.close();
		}
		getGlobalVarStore().clear();

		globalObjects.clear();
		aliases.clear();
		factory = null;
		logger = null;
		objectStore.clear();
		udf.clear();
		debugger = null;
		preProcessor = null;
	}

	private Map<String, Root> compiledCodes = new HashMap<String, Root>();

	public Root compile(final String rawcode) throws ScriptException {
		Root root = compiledCodes.get(rawcode);
		if (root != null) {
			return root;
		}

		String code = preprocess(rawcode);

		List<Token> elements = parse(code);
		root = new Root(this, code);
		if (elements.size() == 0) {
			return root;
		}

		try {
			Token token = elements.get(0);

			root.setStartPos(token.getStartPos());
			root.setEndPos(token.getEndPos());
			Expression a = root;
			Expression temp = null;

			for (int i = 0; i < elements.size(); i++) {

				token = elements.get(i);

				TOKEN_LOOP: {
					String word = token.getWord();
					switch (token.getType()) {

					case PARENTHESES_START:

						if (a.getType() == null) {
							// wenn der Knoten unbenutzt ist, nehmen wir ihn gleich
							a.setParentesesOpen(true);
							a.setStartPos(token.getStartPos());
							a.setEndPos(token.getEndPos());
							a.setType(Type.PARENTHESES);
							a.setTitle("()");
						} else if (a.isFunction()) {
							// Sonderbehandlung Funktion

							temp = a.createSubExpression(token); // ersten Parameter
																	// vorbereiten
							if (a.isParenthesesOpen()) {
								temp.setParentesesOpen(true);
								// oeffende Klammer eines Parameters
								temp.setType(Type.PARENTHESES);
								temp.setStartPos(token.getStartPos());
								a.setWaitingForParam(false); // f(x ; (y)) bringt
																// sonst Fehler
							} else {
								a.setParentesesOpen(true); // oeffnende Klammer der
															// Funktion
								temp.setStartPos(0); // Klammer zaehlt nicht
							}
							a = temp;

						} else {
							a = a.createSubExpression(token);
							// neuer Knoten für diese Klammer
							a.setParentesesOpen(true);
							a.setType(Type.PARENTHESES);
							a.setTitle("()");
						}
						break;

					case PARENTHESES_END:

						while (!a.isParenthesesOpen()) {
							// vielleicht ist hier ein leerer Knoten erzeugt worden
							// z.B.: x := f();
							Expression parent = a.getParent();
							if (a.getType() == null) {
								if (parent.isFunction()) {
									parent.removeSubExpression(a);
								}
							}
							if (parent == null) {
								throw new CompilerException(this, "unexpected.parentheses", a);
							}
							a = parent;
						}
						if (a.isWaitingForParam()) {
							throw new CompilerException(this, "parameter.expected", a);
						}
						a.setParentesesOpen(false);
						a.setEndPos(token.getEndPos());

						break;

					case ALPHANUM:
					case STRING:

						if (a.getType() != null) {
							boolean found = false;
							if (a.isFunction()) {
								if (a.isWaitingForParam() || (a.subCount() == 0)) {
									found = true;
								}
							} else if (a.getType() == Type.OPERATOR) {
								found = true;
							} else if (a.getType() == Type.PARENTHESES) {
								found = true;
							} else if (a.getType() == Type.LIST_SUBSCRIPT) {
								found = true;
							} else if (a.getType() == Type.ROOT) {
								found = true;
							}
							if (found) {
								if (ReservedWords.contains(word)) {
									if (ReservedWords.valueOf(word) == ReservedWords.REM) {
										while (true) {
											i++; // naechstes Wort ignorieren
											if (i >= elements.size()) {
												break;
											}
											token = elements.get(i);
											word = token.getWord();
											if ((token.getType() == ParseType.SEMIKOLON) || (token.getType() == ParseType.PARENTHESES_END)) {
												break;
											}
										}
										if (token.getType() == ParseType.PARENTHESES_END) {
											i--; // schliessende Klammer nicht
											// einfach ueberlesen, sondern
											// nur als Ende des REM-Blocks
											// ansehen
											token = elements.get(i);
											word = token.getWord();
											a.setWaitingForParam(false);
											// im Fall der schliessenden Klammer
											// kann natuerlich kein Parameter mehr
											// folgen
										}
										a.setEndPos(token.getEndPos());
										break; // Goto nextword
									}
								}
								temp = a.createSubExpression(token);
								a.setWaitingForParam(false);
								a = temp;
							} else {
								// zwei Bezeichner nacherinander geht nicht
								// neues Expression-Objekt mit diesem Token generieren
								throw new CompilerException(this, "sequenced.identifiers", root.createSubExpression(token));
							}
						} // (a.getType() != Type.NULL

						a.setTitle(word);
						if (a.getStartPos() == 0) {
							a.setStartPos(token.getStartPos());
						}
						a.setEndPos(token.getEndPos());
						a.setType(Type.ALPHANUM);

						if (token.getType() == ParseType.STRING) {
							a.setSubType(SubType.STRING);
						} else if (a.getTitle().matches("[-+]?\\d+(\\,\\d+)?")) { // is
							// numeric?
							a.setSubType(SubType.NUMBER);
						} else {
							if (i < elements.size() - 1) {
								if (elements.get(i + 1).getType() == ParseType.PARENTHESES_START) {
									a.setSubType(SubType.FUNCTION);
									a.setFunction(true);
									break TOKEN_LOOP; // Goto nextword
								}
							}
							if (ReservedWords.contains(a.getTitle())) {
								a.setStartPos(token.getStartPos());
								switch (ReservedWords.valueOf(a.getTitle())) {
								case FIELD:
									a.setField(true);
									a.setType(null); // auf naechstes Token warten
									break;
								case DEFINE:
									if (i < elements.size() - 1) {
										// wenn in Verbindung eines weiteren
										// Bezeichners verwendet,
										// dann als Schluesselwort interpretieren,
										// sonst als Variable
										if (elements.get(i + 1).getType() == ParseType.ALPHANUM) {
											a.setDefinedFunction(true);
											a.setType(null);
											break TOKEN_LOOP;
										}
									}
									a.setSubType(SubType.VAR);
									break;

								case UNDEFINE:
									if (i < elements.size() - 1) {
										if (elements.get(i + 1).getType() == ParseType.ALPHANUM) {
											a.setUndefine(true);
											a.setType(null);
											break TOKEN_LOOP;
										}
									}
									a.setSubType(SubType.VAR);
									break;

								case GLOBAL:
									if (i < elements.size() - 1) {
										if (elements.get(i + 1).getType() == ParseType.ALPHANUM) {
											a.setGlobal(true);
											a.setType(null);
											break TOKEN_LOOP;
										}
									}
									a.setSubType(SubType.VAR);
									break;

								case ENVIRONMENT:
									a.setEnvironment(true);
									a.setType(null);
									break;

								case OBJECT:
									if (i < elements.size() - 1) {
										if (elements.get(i + 1).getType() == ParseType.ALPHANUM) {
											a.setObject(true);
											a.setType(null);
											break TOKEN_LOOP;
										}
									}
									a.setSubType(SubType.VAR);
									break;

								case LABEL:
									if (i < elements.size() - 1) {
										if (elements.get(i + 1).getType() == ParseType.ALPHANUM) {
											a.setLabel(true);
											a.setType(Type.LABEL);
											// naechstes Wort ist Labelbezeichnung
											i++;
											token = elements.get(i);
											word = token.getWord();
											a.setTitle(word);
											a.setEndPos(token.getEndPos());
											a.getParent().setLabel(a.getTitle(), a.getParent().getElements().size() - 1);
											break TOKEN_LOOP;
										}
									}
									a.setSubType(SubType.VAR);
									break;

								case DEFAULT:
									if (i < elements.size() - 1) {
										if (elements.get(i + 1).getType() == ParseType.ALPHANUM) {
											a.setDefault(true);
											a.setType(null);
											break TOKEN_LOOP;
										}
									}
									a.setSubType(SubType.VAR);
									break;

								case ALIAS:
									if (i < elements.size() - 1) {
										if (elements.get(i + 1).getType() == ParseType.ALPHANUM) {
											a.setAlias(true);
											a.setType(null);
											break TOKEN_LOOP;
										}
									}
									a.setSubType(SubType.VAR);
									break;

								case ORIGINAL:
									if (i < elements.size() - 1) {
										if (elements.get(i + 1).getType() == ParseType.ALPHANUM) {
											a.setOriginal(true);
											a.setType(null);
											break TOKEN_LOOP;
										}
									}
									a.setSubType(SubType.VAR);
									break;

								case REM:
									while (true) {
										i++; // naechstes Wort ignorieren
										if (i >= elements.size()) {
											break;
										}
										token = elements.get(i);
										if ((token.getType() == ParseType.SEMIKOLON) || (token.getType() == ParseType.PARENTHESES_END)) {
											break;
										}
									}
									if (token.getType() == ParseType.PARENTHESES_END) {
										i--; // schliessende Klammer nicht einfach
												// ueberlesen, sondern
										// nur als Ende des REM-Blocks ansehen
										token = elements.get(i);
										a.setWaitingForParam(false);
										// im Fall der schliessenden Klammer kann
										// natuerlich kein Parameter mehr folgen
									}
									a.setEndPos(token.getEndPos());
									break; // Goto nextword

								case CALL:
									a.setCall(true);
									a.setType(null);
									break;

								default:
									throw new CompilerException(this, "unhandled.keyword", a);

								}
							} else {// kein reserv. Wort
								a.setSubType(SubType.VAR);
							}

						}
						break;

					case OPERATOR:

						SubType opTyp;
						if (word.equals("+") || word.equals("-") || word.equals("*+") || word.equals("*-")) {
							opTyp = SubType.OPERATOR_ADD;
						} else if (word.equals("*") || word.equals("/") || word.equals("**") || word.equals("*/")) {
							opTyp = SubType.OPERATOR_MULT;
						} else if (word.equals(":=")) {
							opTyp = SubType.ASSIGNMENT; // Zuweisung auch als
														// Operator betrachen
						} else if (word.equals(":")) {
							opTyp = SubType.OPERATOR_LIST;
						} else if (word.equals("!") || word.equals("&") || word.equals("|") || word.equals("*!")) {
							opTyp = SubType.OPERATOR_LOG;
						} else if (word.equals("<") || word.equals(">") || word.equals("=") || word.equals("!=") || word.equals(">=") || word.equals("<=")
								|| word.equals("=>") || word.equals("=<") || word.equals("=!")) {
							opTyp = SubType.OPERATOR_COMP;
						} else if (word.equals(".")) {
							opTyp = SubType.OPERATOR_DOT;
						} else if (word.equals("*<") || word.equals("*>") || word.equals("*=") || word.equals("*!=") || word.equals("*>=") || word.equals("*<=")
								|| word.equals("*=>") || word.equals("*=<") || word.equals("*=!")) {
							opTyp = SubType.OPERATOR_COMP;
						} else { // wer weiss, was es noch für Operatoren gibt...
							opTyp = SubType.OPERATOR_UNKNOWN;
						}

						if (a.isWaitingForParam()) { // z.B. f(a ; -2)
							temp = a.createSubExpression(token);
							a.setWaitingForParam(false);
							a = temp;
						} else if (a.getType() != null) {
							Expression parent = a.getParent();
							if (parent == null) { // Code beginnt mit einem Operator
								temp = a.createSubExpression(token);
								a = temp;
							} else if (parent.getType() == null) {
								// noch kein Operator vorhanden
								a = parent;
							} else if (a.getType() == Type.OPERATOR) {
								// zwei Operatoren hintereinander, z.B. a := -2
								// in diesem Fall bindet der zweite Operator
								// staerker
								// egal wie er aussieht
								a = a.createSubExpression(token);
							} else if (a.getType() == Type.PARENTHESES && a.isParenthesesOpen()) {
								// z.B. (!a)
								a = a.createSubExpression(token);
							} else if ((parent.getType() != Type.OPERATOR)
									|| ((parent.getSubType() != opTyp) && !parent.getSubType().bindsStrongerThan(opTyp))) {
								// Funktion oder staerkerer Operator
								while (opTyp == SubType.OPERATOR_DOT && parent.getSubType() == SubType.OPERATOR_DOT) {
									// Verkettung: a.b.c(1)
									// neuer Punkt kommt ganz hoch
									a = parent;
									parent = parent.getParent();
								}
								temp = parent.insertSubExpression(a, token.getEndPos());
								if (opTyp == SubType.OPERATOR_DOT) {
									if (a.isCall()) {
										temp.setCall(true);
									}
									a.setObject(true);
								}
								a = temp;
							} else if (opTyp == SubType.ASSIGNMENT) {
								// := sofort ausführen, auch wenn verkettete
								// Zuweisungen
								while (parent.getSubType() == SubType.OPERATOR_DOT) {
									// o.method1.method2 := x anders als 1 + a:=2
									// behandeln
									a = parent;
									parent = parent.getParent();
								}
								temp = parent.insertSubExpression(a, token.getEndPos());
								a = temp;
							} else {// Knoten muss weiter oben im Baum eingefügt
									// werden
								while ((parent.getParent() != null) && ((parent.getType() != Type.OPERATOR) || !opTyp.bindsStrongerThan(parent.getSubType()))
										&& !parent.isParenthesesOpen()) {
									// aufwaerst wandern, bis Bindung nicht mehr
									// staerker ist
									a = parent;
									parent = parent.getParent();
								}
								temp = parent.insertSubExpression(a, token.getEndPos());
								if (opTyp == SubType.OPERATOR_DOT) {
									a.setObject(true);
								}
								a = temp;
							}
						}
						a.setType(Type.valueOf(token.getType().toString()));
						a.setSubType(opTyp);
						a.setTitle(word);
						if (a.getStartPos() == 0) {
							// kann bei "!cond" passieren
							a.setStartPos(token.getStartPos());
						}
						a.setEndPos(token.getEndPos());
						break;

					case SEMIKOLON:
						if (a.getParent() == null) {
							if (a.isFunction() && a.isParenthesesOpen()) {
								throw new CompilerException(this, "missing.parentheses", a);
							}
						} else {
							boolean found = false;
							while (!found) {
								if ((a.isFunction() || a.getType() == Type.PARENTHESES) && a.isParenthesesOpen()) {
									found = true;
								} else {
									if (a.isParenthesesOpen()) {
										throw new CompilerException(this, "missing.parentheses", a);
									}
									if (a.getParent() == null) {
										found = true;
									} else {
										a = a.getParent();
									}
								}
							}
						}
						a.setWaitingForParam(true);
						break;

					case LIST_SUBSCRIPT_START:
						if (a.getType() == null) {
							// wenn der Knoten unbenutzt ist, nehmen wir ihn gleich.
							// Dann ist das ein KeyWord [OK] oder Zeitkonstante
							a.setType(Type.KEYWORD_OR_TIME);
							a.setStartPos(token.getStartPos());
						} else if (a.getType() == Type.ROOT) {
							a = a.createSubExpression(token);
							a.setType(Type.KEYWORD_OR_TIME);
						} else if (a.getType() == Type.OPERATOR) {
							// Verknuepfung zweier KeyWords/Zeitkonstanten
							a = a.createSubExpression(token);
							a.setType(Type.KEYWORD_OR_TIME);
						} else if (a.isWaitingForParam()) {
							// Keyword als Funktionsparameter
							a.setWaitingForParam(false);
							a = a.createSubExpression(token);
							a.setType(Type.KEYWORD_OR_TIME);
						} else { // neuer Knoten Index-Operator
							if (a.getParent().getSubType().equals(SubType.OPERATOR_DOT)) {
								// wenn das eine Methode ist, z.B. o.method[1]
								boolean found = false; // bis zum obersten DOT
														// hangeln
								while (!found) {
									if (!a.getParent().getSubType().equals(SubType.OPERATOR_DOT)) {
										found = true;
									} else {
										a = a.getParent();
									}
								}
							}
							a = a.getParent().insertSubExpression(a, token.getEndPos());
							a.setParentesesOpen(true);
							a.setWaitingForParam(true);
							a.setType(Type.LIST_SUBSCRIPT);
							a.setTitle("[]");
						}
						if (a.getType() == Type.KEYWORD_OR_TIME) {
							StringBuilder sb = new StringBuilder();
							while (token.getType() != ParseType.LIST_SUBSCRIPT_END) {
								if (token.getType() == ParseType.ALPHANUM) {
									if (elements.get(i - 1).getType() == ParseType.ALPHANUM) {
										sb.append(" ");
									}
								}
								sb.append(word);
								i++;
								if (i >= elements.size()) {
									throw new CompilerException(this, "missing.parentheses", a);
								}
								token = elements.get(i);
								word = token.getWord();
							}
							sb.append(token.getWord());
							a.setTitle(sb.toString()); // Klammer schliessen
							a.setEndPos(token.getEndPos());

							if (KeyWords.contains(a.getTitle())) {
								a.setType(Type.KEYWORD);
								// a.setKeyWord(KeyWords.valueOf(word));
							} else {
								a.setType(Type.TIME);
							}
						}
						break;

					case LIST_SUBSCRIPT_END:
						while (true) {
							if (a.getParent() == null) {
								break;
							}
							a = a.getParent();
							if (a.isParenthesesOpen()) {
								break;
							}
						}
						if (!a.isParenthesesOpen()) {
							throw new CompilerException(this, "unexpected.parentheses", a);
						} else if (a.isWaitingForParam()) {
							throw new CompilerException(this, "parentheses.expected", a);
						}
						a.setParentesesOpen(false);
						a.setEndPos(token.getEndPos());
						break;

					case OPERATOR_SEARCH:
						a.setSearch(true);
						a.setEndPos(token.getEndPos());
						break;

					default:
						break;
					}
				} // LABEL
			}

			// offene Klammern suchen
			while (true) {
				if (a.isParenthesesOpen()) {
					throw new CompilerException(this, "missing.parentheses", a);
				}
				a = a.getParent();
				if (a == null) {
					break;
				}
			}

			compiledCodes.put(rawcode, root); // cachen
			return root;
		} catch (Throwable e) {
			throw new CompilerException(this, root, e) {
				private static final long serialVersionUID = 1L;

				@Override
				public String getMessage() {
					return XflEngine.this.getMessage("compile.error", rawcode);
				}

			};
		}
	}

	private String preprocess(String rawcode) {
		if (preProcessor == null) {
			return rawcode;
		}
		return preProcessor.preprocess(rawcode);
	}

	public String getAlias(String key) {
		return aliases.get(key);
	}

	/**
	 * Anmeldung eines Evaluators<br/>
	 * 
	 * @param extension
	 */
	public void addExtension(XflExtension ex) {
		addEvaluator(ex.getFunctionNames(), new ExtensionEvaluator(this, ex));
	}

	/**
	 * 
	 * registriert einen Evaluator fuer bestimmte @Formeln<br>
	 * Formelnamen koennen kommasepariert uebergeben werden.
	 * 
	 * @param formulaName
	 * @param evaluator
	 */
	public void addEvaluator(String formulaName, Evaluator evaluator) {
		String[] functions = formulaName.split(",");
		for (String f : functions) {
			userDefinedEvaluators.put(f.toUpperCase(), evaluator);
		}
	}

	/**
	 * Ermittelt die in LotusScript gehaltenen Erweiterungen und registriert diese
	 * 
	 * @throws Exception
	 */
	public void addLotusScriptExtensions() throws Exception {
		LotusScriptEvaluator ev = new LotusScriptEvaluator(this);
		ev.scanLibs();
		// bestimmte Funktionen standardmaessig per LS evaluieren
		addEvaluator("@EXECUTE", ev);
	}

	public Evaluator getExpressionEvaluator(Expression expression) throws EvaluationException {

		Evaluator ev = null;
		String title = expression.getTitle();

		// sind Evaluatoren registriert?
		if (userDefinedEvaluators.containsKey(title)) {
			return userDefinedEvaluators.get(title);
		}
		if (defaultEvaluators.containsKey(title)) {
			return defaultEvaluators.get(title);
		}

		switch (expression.getType()) {

		case OPERATOR:
			switch (expression.getSubType()) {
			case OPERATOR_ADD:
			case OPERATOR_MULT:
			case OPERATOR_COMP:
			case OPERATOR_LIST:
				ev = new BinaryOperatorEvaluator(this);
				break;

			case OPERATOR_LOG:
				ev = new LogicalOperatorEvaluator(this);
				break;

			case ASSIGNMENT:
				ev = new AssignmentEvaluator(this);
				break;

			case OPERATOR_DOT:
				ev = new DotEvaluator(this);
				break;

			default:
				throw new EvaluationException(this, expression, "no.evaluator.found");
			}
			defaultEvaluators.put(title, ev); // merken
			break;

		case ALPHANUM:

			if (defaultEvaluators.containsKey(EVALUATOR_ALPHANUM)) {
				return defaultEvaluators.get(EVALUATOR_ALPHANUM);
			}
			ev = new AlphanumEvaluator(this);
			defaultEvaluators.put(EVALUATOR_ALPHANUM, ev); // merken
			return ev;

		case PARENTHESES:
			ev = new ParenthesesEvaluator(this);
			defaultEvaluators.put(title, ev); // merken
			break;

		case KEYWORD:
			// return new Evaluator(expression);
			throw new EvaluationException(this, expression, "no.evaluator.found");

		case LIST_SUBSCRIPT:
			ev = new IndexEvaluator(this);
			defaultEvaluators.put(title, ev); // merken
			break;

		case LABEL:
			if (defaultEvaluators.containsKey(EVALUATOR_LABEL)) {
				return defaultEvaluators.get(EVALUATOR_LABEL);
			}
			ev = new LabelEvaluator(this);
			defaultEvaluators.put(EVALUATOR_LABEL, ev); // merken
			break;

		case ROOT:
			ev = new RootEvaluator(this);
			defaultEvaluators.put(Root.TITLE, ev); // merken
			break;

		case TIME:
			if (defaultEvaluators.containsKey(EVALUATOR_CONSTANT)) {
				return defaultEvaluators.get(EVALUATOR_CONSTANT);
			}
			ev = new ConstantEvaluator(this);
			defaultEvaluators.put(EVALUATOR_CONSTANT, ev); // merken
			break;

		default:
			throw new EvaluationException(this, expression, "no.evaluator.found");
		}

		return ev;
	}

	public Map<String, Object> getObjectStore() {
		return objectStore;
	}

	public boolean hasGlobalObject(String name) {
		return globalObjects.containsKey(name.toUpperCase());
	}

	public Data getGlobalObjectWrapper(String name) {
		return globalObjects.get(name.toUpperCase());
	}

	public Object getGlobalObject(String name) {
		Data wrapper = getGlobalObjectWrapper(name);
		return wrapper == null ? null : wrapper.getObject();
	}

	public void setGlobalObject(String name, Object o) {
		if (o instanceof Data) {
			globalObjects.put(name.toUpperCase(), (Data) o);
		} else {
			Data res = new Data(this);
			try {
				res.assignObject(o);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			globalObjects.put(name.toUpperCase(), res);
		}
	}

	public void removeGlobalObject(String name) {
		globalObjects.remove(name.toUpperCase());
	}

	public Expression getUDF(String name, int paramCount) {
		return udf.get(name + "~#" + paramCount);
	}

	public void setGlobalVar(String name, Object var) throws Exception {
		getBindings(0).put(name, var);
	}

	public boolean hasGlobalVar(String name) {
		return getBindings(0).containsKey(name);
	}

	public Data getGlobalVar(String name) {
		return getBindings(0).getData(name);
	}

	public Map<String, Data> getAllGlobalVars() {
		return getBindings(0).getAll();
	}

	public void removeGlobalVar(String name) {
		getBindings(0).remove(name);
	}

	public boolean isAliasDefined(String key) {
		return aliases.containsKey(key);
	}

	public boolean isUDFDefined(String name, int paramCount) {
		return udf.containsKey(name + "~#" + paramCount);
	}

	// Spaces, Umbrüche, Tabs werden ignoriert
	// Non-breaking space 160 = xA0
	private final static String spaceNewlineTabEtc = "[ \\n\\r\\t\\xA0]";

	/**
	 * Zerlegung des Quellcodes in Token
	 * 
	 * @param code
	 * @return Liste von Token
	 * @throws Exception
	 */
	private List<Token> parse(String code) throws ScriptException {
		List<Token> elements = new ArrayList<Token>();
		if (code == null) {
			return elements;
		}
		ParseType mode = ParseType.NULL;
		ParseType typ = ParseType.NULL;
		String endString = null;
		int tempPos = 0;
		StringBuilder word = new StringBuilder();
		int pos = 0;

		for (pos = 1; pos <= code.length(); pos++) {
			char c = code.charAt(pos - 1);
			String z = Character.toString(c);

			boolean isSpaceNewlineTabEtc = z.matches(spaceNewlineTabEtc);
			if (isSpaceNewlineTabEtc) {
				// ignorieren
			} else if ("\"".equals(z)) {
				if (!mode.equals(ParseType.STRING_START)) {
					tempPos = pos;
					typ = ParseType.STRING_START;
					endString = "\"";
				}
			} else if ("(".equals(z)) {
				typ = ParseType.PARENTHESES_START;
			} else if (")".equals(z)) {
				typ = ParseType.PARENTHESES_END;
			} else if (z.matches("[0-9a-zA-Z,@_äÄöÖüÜß$~%§]")) {
				typ = ParseType.ALPHANUM;
			} else if (z.matches("[\\+\\-\\*\\/:!&,|<>!=\\.]")) {
				typ = ParseType.OPERATOR;
			} else if (";".equals(z)) {
				typ = ParseType.SEMIKOLON;
			} else if ("{".equals(z)) {
				if (!mode.equals(ParseType.STRING_START)) {
					tempPos = pos;
					typ = ParseType.STRING_START;
					endString = "}";
				}
			} else if ("'".equals(z)) {
				if (!mode.equals(ParseType.STRING_START)) {
					tempPos = pos;
					typ = ParseType.STRING_START;
					endString = "'";
				}
			} else if ("?".equals(z)) {
				typ = ParseType.OPERATOR_SEARCH;
			} else if ("[".equals(z)) {
				typ = ParseType.LIST_SUBSCRIPT_START;
			} else if ("]".equals(z)) {
				typ = ParseType.LIST_SUBSCRIPT_END;
			} else {
				// alle Sonderzeichen
				typ = ParseType.STRING;
			}

			if (mode.equals(ParseType.STRING_START)) {
				word.append(z);
				if ("\\".equals(z)) {
					pos++;
					c = code.charAt(pos - 1);
					z = Character.toString(c);
					word.append(z);
				} else if (endString.equals(z)) {
					mode = ParseType.STRING;
					addToken(elements, word.toString(), mode, tempPos);
					tempPos = 0;
					mode = ParseType.NULL;
					word.setLength(0);
				}
			} else {
				if (isSpaceNewlineTabEtc) {
					if (mode != ParseType.NULL) {
						addToken(elements, word.toString(), mode, tempPos);
						tempPos = 0;
						mode = ParseType.NULL;
						word.setLength(0);
					}
				} else {
					if (mode == ParseType.NULL) {
						mode = typ;
						word.setLength(0);
						word.append(z);
						tempPos = pos;
					} else if ((typ == ParseType.PARENTHESES_START || typ == ParseType.PARENTHESES_END) && (typ == mode)) {
						addToken(elements, word.toString(), mode, tempPos);
						tempPos = pos;
					} else if (typ != mode) {
						addToken(elements, word.toString(), mode, tempPos);
						mode = typ;
						word.setLength(0);
						word.append(z);
						tempPos = pos;
					} else {
						word.append(z);
					}
				}
			}

		}

		if (word.length() > 0) {
			addToken(elements, word.toString(), typ, tempPos);
			tempPos = 0;
		}
		return elements;
	}

	private boolean debugMode = false;

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	private XFLDebugger debugger = null;

	public XFLDebugger getDebugger() {
		return debugger;
	}

	public void setDebugger(XFLDebugger debugger) {
		this.debugger = debugger;
	}

	/**
	 * wird von jedem Evaluator aufgerufen, wenn isDebugMode()
	 * 
	 * @param exp
	 * @param result
	 */
	public void debug(Expression exp, Data result) {
		if (debugger == null) {
			debugger = new SysOutDebugger(this);
		}
		try {
			debugger.debug(exp, result);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void setAlias(String alias, String word) {
		aliases.put(alias, word);
	}

	public void setUDF(String name, Expression expression) {
		// Assignment > (0) Function / (1) Body > Parameter (0..n)
		udf.put(name + "~#" + expression.getElement(0).getElements().size(), expression);
	}

	public void removeUDF(String name, int paramCount) {
		udf.remove(name + "~#" + paramCount);
	}

	/**
	 * liefert den passenden Evaluator zu einer Funktion wenn kein spezieller Evaluator bekannt ist, liefert diese Methode null
	 * 
	 * @param functionName
	 *            z.B. @MyFunction
	 * @param paramCount
	 *            Signatur der Funktion (Anzahl der Parameter)
	 * @return {@link Evaluator}
	 */
	public Evaluator getEvaluator(String functionName, int paramCount) {
		String uName = functionName.toUpperCase();
		// sind Evaluatoren registriert?
		String key = uName + "~#" + paramCount;
		if (functionEvaluators.containsKey(key)) {
			return functionEvaluators.get(key);
		}
		Evaluator ev = null;
		// wir lassen sowohl @If() als auch If() zu
		String uNameOhneAt = uName.startsWith("@") ? uName.substring(1, uName.length()) : uName;

		if ("IF".equals(uNameOhneAt)) {
			ev = new IfEvaluator(this);

		} else if ("DO".equals(uNameOhneAt)) {
			ev = new DoEvaluator(this);

		} else if ("SET".equals(uNameOhneAt)) {
			ev = new SetEvaluator(this);

		} else if ("SETFIELD".equals(uNameOhneAt)) {
			ev = new SetFieldEvaluator(this);

		} else if ("SETGLOBAL".equals(uNameOhneAt)) {
			ev = new SetGlobalEvaluator(this);

		} else if ("SETOBJECT".equals(uNameOhneAt)) {
			ev = new SetObjectEvaluator(this);

		} else if ("SETGLOBALOBJECT".equals(uNameOhneAt)) {
			ev = new SetGlobalObjectEvaluator(this);

		} else if ("GET".equals(uNameOhneAt)) {
			ev = new GetEvaluator(this);

		} else if ("GETFIELD".equals(uNameOhneAt)) {
			ev = new GetFieldEvaluator(this);

		} else if ("GETGLOBAL".equals(uNameOhneAt)) {
			ev = new GetGlobalEvaluator(this);

		} else if ("GETOBJECT".equals(uNameOhneAt)) {
			ev = new GetObjectEvaluator(this);

		} else if ("GETGLOBALOBJECT".equals(uNameOhneAt)) {
			ev = new GetGlobalObjectEvaluator(this);

		} else if ("GETDOCFIELD".equals(uNameOhneAt)) {
			ev = new GetDocFieldEvaluator(this);

		} else if ("SETDOCFIELD".equals(uNameOhneAt)) {
			ev = new SetDocFieldEvaluator(this);

		} else if ("FOR".equals(uNameOhneAt)) {
			ev = new ForEvaluator(this);

		} else if ("WHILE".equals(uNameOhneAt)) {
			ev = new WhileEvaluator(this);

		} else if ("DOWHILE".equals(uNameOhneAt)) {
			ev = new DoWhileEvaluator(this);

		} else if ("RETURN".equals(uNameOhneAt)) {
			ev = new ReturnEvaluator(this);

		} else if ("GOTO".equals(uNameOhneAt)) {
			ev = new GotoEvaluator(this);

		} else if ("GOSUB".equals(uNameOhneAt)) {
			ev = new GosubEvaluator(this);

		} else if ("PRINT".equals(uNameOhneAt)) {
			ev = new PrintEvaluator(this);

		} else if ("PROMPT".equals(uNameOhneAt)) {
			ev = new PromptEvaluator(this);

		} else if ("NEWLINE".equals(uNameOhneAt)) {
			ev = new NewLineEvaluator(this);

		} else if ("TRANSFORM".equals(uNameOhneAt)) {
			ev = new TransformEvaluator(this);

		} else if ("EVAL".equals(uNameOhneAt)) {
			ev = new EvalEvaluator(this);

		} else if ("ISAVAILABLE".equals(uNameOhneAt)) {
			ev = new IsAvailableEvaluator(this);

		} else if ("ISUNAVAILABLE".equals(uNameOhneAt)) {
			ev = new IsUnAvailableEvaluator(this);

		} else if ("ISDEFINED".equals(uNameOhneAt)) {
			ev = new IsDefinedEvaluator(this);

		} else if ("ERROR".equals(uNameOhneAt)) {
			ev = new ErrorEvaluator(this);

		} else if ("ISNOTHING".equals(uNameOhneAt)) {
			ev = new IsNothingEvaluator(this);

		} else if ("NOTHING".equals(uNameOhneAt)) {
			ev = new NothingEvaluator(this);

		} else if ("DELETEFIELD".equals(uNameOhneAt)) {
			ev = new DeleteFieldEvaluator(this);

		} else if ("DELETEDOCUMENT".equals(uNameOhneAt)) {
			ev = new DeleteDocumentEvaluator(this);

		} else if ("SAVEDOCUMENT".equals(uNameOhneAt)) {
			ev = new SaveDocumentEvaluator(this);

		} else if ("DEBUG".equals(uNameOhneAt)) {
			ev = new DebugEvaluator(this);

		} else if ("ISJAVARUNTIME".equals(uNameOhneAt)) {
			ev = new IsJavaRuntimeEvaluator(this);

		} else if ("ISLOTUSSCRIPTRUNTIME".equals(uNameOhneAt)) {
			ev = new IsLotusScriptRuntimeEvaluator(this);

		} else if ("XFLVERSION".equals(uNameOhneAt)) {
			ev = new XflVersionEvaluator(this);

		} else if ("RECYCLE".equals(uNameOhneAt)) {
			ev = new RecycleEvaluator(this);

		} else if ("HTMLDECODE".equals(uNameOhneAt)) {
			ev = new HtmlDecodeEvaluator(this);

		} else if ("HTMLENCODE".equals(uNameOhneAt)) {
			ev = new HtmlEncodeEvaluator(this);

		} else if ("TRY".equals(uNameOhneAt)) {
			ev = new TryEvaluator(this);

		} else if ("RUNAGENT".equals(uNameOhneAt)) {
			ev = new RunAgentEvaluator(this);

		} else if ("ISNEWDOC".equals(uNameOhneAt)) {
			ev = new IsNewDocEvaluator(this);

		} else if ("BASE64ENCODE".equals(uNameOhneAt)) {
			ev = new Base64EncodeEvaluator(this);

		} else if ("BASE64DECODE".equals(uNameOhneAt)) {
			ev = new Base64DecodeEvaluator(this);

		} else if ("EXECUTE".equals(uNameOhneAt)) {
			ev = new ExecuteEvaluator(this);

		} else if ("SETREFDOC".equals(uNameOhneAt)) {
			ev = new SetRefDocEvaluator(this);

		} else if ("NORMALIZE".equals(uNameOhneAt)) {
			ev = new NormalizeEvaluator(this);

		} else if ("NORMALIZEEXCEPTUMLAUT".equals(uNameOhneAt)) {
			ev = new NormalizeExceptUmlautEvaluator(this);

		} else if ("CREATEJAVAOBJECT".equals(uNameOhneAt)) {
			ev = new CreateJavaObjectEvaluator(this);

		} else if ("HEXENCODE".equals(uNameOhneAt)) {
			ev = new HexEncodeEvaluator(this);

		} else if (uName.startsWith("@") || paramCount > 0) {
			ev = getDefaultEvaluator();
		}

		if (ev != null) {
			functionEvaluators.put(key, ev);
		}
		return ev;
	}

	/**
	 * liefert den Standard, wenn nichts fuer die Funktion definiert wurde
	 * 
	 * @return {@link Evaluator}
	 */
	public Evaluator getDefaultEvaluator() {
		if (defaultEvaluators.containsKey(EVALUATOR_SFL)) {
			return defaultEvaluators.get(EVALUATOR_SFL);
		}
		NotesFormulaEvaluator ev = new NotesFormulaEvaluator(this);
		defaultEvaluators.put(EVALUATOR_SFL, ev);
		return ev;
	}

	/**
	 * wird gesetzt, wenn @Gosub laeuft
	 */
	private String searchLabel = null;

	public String getSearchLabel() {
		return searchLabel;
	}

	public void setSearchLabel(String label) {
		this.searchLabel = label;
	}

	public Object eval(String code, Document refDoc) throws ScriptException {
		Root root = compile(code);
		Context context = createContext(root, refDoc);
		try {
			return context.evaluate();
		} finally {
			context.close();
			removeUnusedItems();
		}
	}

	public Context createContext(Expression root, Document refDoc) {
		return new Context(root, refDoc);
	}

	// Interface ScriptEngine

	@Override
	public Bindings createBindings() {
		return bindings;
	}

	@Override
	public Object eval(String script) throws ScriptException {
		return eval(script, (Document) null);
	}

	@Override
	@Deprecated
	public Object eval(Reader reader) throws ScriptException {
		throw new ScriptException("TODO");
	}

	@Override
	@Deprecated
	public Object eval(String script, ScriptContext context) throws ScriptException {
		throw new ScriptException("TODO");
	}

	@Override
	@Deprecated
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		throw new ScriptException("TODO");
	}

	@Override
	@Deprecated
	public Object eval(String script, Bindings n) throws ScriptException {
		throw new ScriptException("TODO");
	}

	@Override
	@Deprecated
	public Object eval(Reader reader, Bindings n) throws ScriptException {
		throw new ScriptException("TODO");
	}

	@Override
	public Object get(String key) {
		Data data = getGlobalVar(key);
		if (data != null) {
			try {
				return data.getValue();
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public XflBindings getBindings(int scope) {
		return bindings;
	}

	private GlobalVarStore varStore = null;

	public GlobalVarStore getGlobalVarStore() {
		if (varStore == null) {
			varStore = new GlobalVarStore(this);
		}
		return varStore;
	}

	@Override
	public ScriptContext getContext() {
		return null;
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	@Override
	public void put(String key, Object value) {
		try {
			setGlobalVar(key, value);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {

	}

	@Override
	public void setContext(ScriptContext context) {

	}

	// validieren des uebergebenen Strings, ob valide Formel
	// Fehler, wenn nicht valide mit Information
	/**
	 * Validiert, ob die angegebene Formel syntaktisch Korrekt ist.
	 * 
	 * @param action
	 *            String mit Formel
	 * @throws LException
	 *             Fehlerinformation, wenn invalide
	 */
	public void validate(String action) throws LException {
		if (action == null) {
			return;
		}

		try {
			compile(action);
		} catch (ScriptException e) {
			throw new LException(this, e);
		}
	}

	@Deprecated
	public final void setLicenseKey(String licenseKey) {
	}

	private ILogger logger = null;

	public final ILogger getLogger() {
		return logger;
	}

	public final void setLogger(ILogger logger) {
		// if (logger != null) {
		// logger.log("cannot change logger");
		// } else {
		this.logger = logger;
		// }
	}

	public final void log(String text) {
		if (logger != null) {
			logger.log(text);
		} else {
			System.out.println(text);
		}
	}

	// ***************** Sprache *********************

	private Locale locale;

	private ResourceBundle bundleMsg;

	public void setLanguage(String language) throws Exception {
		setLocale(new Locale(language));
	}

	public Locale getLocale() {
		return locale == null ? getDefaultLocale() : locale;
	}

	public Locale getDefaultLocale() {
		return Locale.getDefault();
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
		bundleMsg = null;
	}

	private ResourceBundle getBundleMessages() {
		if (bundleMsg == null) {
			Locale loc = getLocale();
			if (loc == null) {
				bundleMsg = ResourceBundle.getBundle(RESOURCE_MESSAGES, new ResourceBundleControl());
			} else {
				bundleMsg = ResourceBundle.getBundle(RESOURCE_MESSAGES, loc, new ResourceBundleControl());
			}
		}
		return bundleMsg;
	}

	private class ResourceBundleControl extends ResourceBundle.Control {

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale) {
			// falls deutsche Locale angefordert -> Fallback auf leer
			if (locale.getLanguage().equals("de") && locale.getCountry().equals("")) {
				return new Locale("");
			}
			return super.getFallbackLocale(baseName, locale);
		}

	}

	/**
	 * liefert den Text zu einem Schluessel<br>
	 * Aufloesung ueber ResourceBundle
	 * 
	 * @param key
	 * @return String mit Nachricht
	 */
	public String getMessage(String key) {
		try {
			return getBundleMessages().getString(key);
		} catch (Exception ignore) {
			return key;
		}
	}

	/**
	 * Liefert den Text zu einem Schluessel incl. Variablen<br>
	 * Aufloesung ueber ResourceBundle
	 * 
	 * @param stringName
	 * @param args
	 * @return String mit Nachricht
	 */
	public String getMessage(String stringName, Object... args) {
		String msg = getMessage(stringName);
		try {
			return MessageFormat.format(msg, args).trim();
		} catch (Exception ignore) {
			// zur Not alle Parameter nacheinander ausgeben
			for (int i = 0; i < args.length; i++) {
				if (!"".equals(args[i])) {
					msg += " ";
					msg += args[i];
				}
			}
			return msg;
		}
	}

	private Preprocessor preProcessor = null;

	public Preprocessor getPreprocessor() {
		return preProcessor;
	}

	public void setPreprocessor(Preprocessor preprocessor) {
		this.preProcessor = preprocessor;
	}

	private List<String> unusedItems = new ArrayList<String>();

	public void registerUnusedItem(String text) {
		modifyUnusedList(ACTION_ADD, text);
	}

	private final byte ACTION_ADD = 1;
	private final byte ACTION_REMOVE = 2;

	protected synchronized void modifyUnusedList(byte action, String text) {
		if (action == ACTION_ADD) {
			unusedItems.add(text);
		} else {
			unusedItems.remove(text);
		}
	}

	/**
	 * die Feldnamen werden vom Finalizer der Data-Objekte angemeldet
	 */
	public void removeUnusedItems() {
		if (unusedItems.size() > 0) {
			List<String> unusedItems2 = new ArrayList<String>(unusedItems);
			Document doc = getGlobalVarStore().getDoc();
			for (String item : unusedItems2) {
				try {
					Data.releaseItemName(item);
					doc.removeItem(item);
					modifyUnusedList(ACTION_REMOVE, item);
				} catch (NotesException e) {
				}
			}
		}
	}
}