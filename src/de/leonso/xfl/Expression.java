package de.leonso.xfl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.script.ScriptException;

import de.leonso.xfl.evaluators.Evaluator;
import de.leonso.xfl.exceptions.EvaluationException;
import de.leonso.xfl.exceptions.LabelNotDefinedException;

public class Expression implements Serializable {
	private static final long serialVersionUID = 1L;

	private String title = null;
	private Type type = null;
	private SubType subType = SubType.NULL;

	private int startPos = 0;
	private int endPos = 0;

	private Expression parent = null;
	private final ArrayList<Expression> subExpressions = new ArrayList<Expression>();
	protected Root root = null;
	private final HashMap<String, Integer> labels = new HashMap<String, Integer>();

	// flags
	private boolean flagParenthesesOpen = false;
	private boolean flagFunction = false;
	private boolean flagDefinedFunction = false;
	private boolean flagUndefine = false;
	private boolean flagField = false;
	private boolean flagGlobal = false;
	private boolean flagEnvironment = false;
	private boolean flagObject = false;
	private boolean flagLabel = false;
	private boolean flagDefault = false;
	private boolean flagWaitingForParam = false;
	private boolean flagAlias = false;
	private boolean flagOriginal = false;
	private boolean flagCall = false;
	private boolean flagSearch = false;

	public Expression() {
		// if (ce != null) {
		// this.startPos = ce.getStartPos();
		// this.endPos = ce.getEndPos();
		// this.title = ce.getWord();
		// }
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public SubType getSubType() {
		return subType;
	}

	public void setSubType(SubType subType) {
		this.subType = subType;
	}

	public Integer getStartPos() {
		return startPos;
	}

	public void setStartPos(Integer startPos) {
		this.startPos = startPos;
	}

	public void setEndPos(Integer endPos) {
		this.endPos = endPos;
		// nach oben durchreichen
		if (parent != null) {
			parent.setEndPos(endPos);
		}
	}

	public Integer getEndPos() {
		return endPos;
	}

	public Expression getParent() {
		return parent;
	}

	public void setParent(Expression parent) {
		this.parent = parent;
	}

	public ArrayList<Expression> getElements() {
		return subExpressions;
	}

	public Root getRoot() {
		return root;
	}

	public XflEngine getEngine() {
		return getRoot().getEngine();
	}

	public void setRoot(Root root) {
		this.root = root;
	}

	public boolean isParenthesesOpen() {
		return flagParenthesesOpen;
	}

	public void setParentesesOpen(boolean flag) {
		this.flagParenthesesOpen = flag;
	}

	public boolean isFunction() {
		return flagFunction;
	}

	public void setFunction(boolean flag) {
		this.flagFunction = flag;
	}

	public boolean isWaitingForParam() {
		return flagWaitingForParam;
	}

	public void setWaitingForParam(boolean waitingForParam) {
		this.flagWaitingForParam = waitingForParam;
	}

	public boolean isDefinedFunction() {
		return flagDefinedFunction;
	}

	public void setDefinedFunction(boolean definedFunction) {
		this.flagDefinedFunction = definedFunction;
	}

	public boolean isUndefine() {
		return flagUndefine;
	}

	public void setUndefine(boolean undefine) {
		this.flagUndefine = undefine;
	}

	public boolean isGlobal() {
		return flagGlobal;
	}

	public void setGlobal(boolean global) {
		this.flagGlobal = global;
	}

	public boolean isEnvironment() {
		return flagEnvironment;
	}

	public void setEnvironment(boolean environment) {
		this.flagEnvironment = environment;
	}

	public boolean isField() {
		return flagField;
	}

	public void setField(boolean field) {
		this.flagField = field;
	}

	public boolean isObject() {
		return flagObject;
	}

	public void setObject(boolean object) {
		this.flagObject = object;
	}

	public boolean isCall() {
		return flagCall;
	}

	public void setCall(boolean call) {
		this.flagCall = call;
	}

	public boolean isLabel() {
		return flagLabel;
	}

	public void setLabel(boolean label) {
		this.flagLabel = label;
	}

	public boolean isDefault() {
		return flagDefault;
	}

	public void setDefault(boolean defaultKw) {
		this.flagDefault = defaultKw;
	}

	public boolean isAlias() {
		return flagAlias;
	}

	public void setAlias(boolean alias) {
		this.flagAlias = alias;
	}

	public boolean isOriginal() {
		return flagOriginal;
	}

	public void setOriginal(boolean original) {
		this.flagOriginal = original;
	}

	public boolean isSearch() {
		return flagSearch;
	}

	public void setSearch(boolean search) {
		this.flagSearch = search;
	}

	public Expression createSubExpression(Token token) {
		Integer startPos = token.getStartPos();
		Integer endPos = token.getEndPos();
		Expression e = new Expression(); // Factory.createInstanceOf_Expression(token.getType(),
											// token.getWord());
		e.setRoot(getRoot());
		e.setStartPos(startPos);
		e.setEndPos(endPos);
		addSubExpression(e, endPos);
		return e;
	}

	// f�gt einen Knoten oberhalb des uebergebenen ein
	public Expression insertSubExpression(Expression e, Integer endPos) {
		Expression temp = new Expression();
		temp.setRoot(getRoot());
		this.addSubExpression(temp, endPos);
		temp.addSubExpression(e, endPos);
		// Klammerstatus des Unterknotens hochziehen
		boolean parenthesesOpen = e.isParenthesesOpen();
		if (parenthesesOpen) {
			temp.setParentesesOpen(true);
			e.setParentesesOpen(false);
		}
		// linker Rand des neuen ist gleich dem des alten Unterknotens
		temp.setStartPos(e.getStartPos());
		subExpressions.remove(e);
		return temp;
	}

	public void addSubExpression(Expression e, Integer endPos) {
		subExpressions.add(e);
		e.setParent(this);
		setEndPos(endPos);
	}

	public int subCount() {
		return subExpressions.size();
	}

	public void setLabel(String name, Integer pos) {
		labels.put(name, pos);
	}

	public Data evaluate(Context rti) throws ScriptException {
		XflEngine engine = getEngine();
		Evaluator evaluator = engine.getExpressionEvaluator(this);
		try {
			Data res = evaluator.evaluate(this, rti);
			if (engine.isDebugMode()) {
				engine.debug(this, res);
			}
			return res;
		} catch (EvaluationException e) {
			throw e;
		} catch (Throwable e) {
			throw new EvaluationException(engine, this, e);
		}
	}

	public Expression getElement(int i) {
		return subExpressions.get(i);
	}

	public String getCode() {
		return getRoot().getCode().substring(startPos - 1, endPos);
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public void setEndPos(int endPos) {
		this.endPos = endPos;
		if (parent != null) {
			if (parent.endPos < this.endPos) {
				parent.setEndPos(endPos);
			}
		}
	}

	@Override
	public String toString() {
		return getCode();
	}

	public boolean hasLabel(String label) {
		return labels.containsKey(label);
	}

	public int getLabelPosition(String label) throws LabelNotDefinedException {
		Integer pos = labels.get(label);
		if (pos == null) {
			throw new LabelNotDefinedException(getEngine(), this, label);
		}
		return pos;
	}

	public void removeSubExpression(Expression expression) {
		subExpressions.remove(expression);
	}

}
