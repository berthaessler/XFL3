package de.leonso.xfl.exceptions;

import javax.script.ScriptException;

import de.leonso.xfl.XflEngine;

/**
 * Grundlage fuer alle Exceptions
 * 
 * @author Bert
 *
 */
public abstract class XflException extends ScriptException {
	private static final long serialVersionUID = 1L;

	private XflEngine engine;

	public XflException(XflEngine engine, String msg) {
		super(msg);
		this.engine = engine;
	}

	public XflException(XflEngine engine, Throwable cause) {
		super(cause instanceof Exception ? (Exception) cause : new Exception(cause));
		this.engine = engine;
	}

	public XflEngine getEngine() {
		return engine;
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		return engine.getMessage(msg);
	}

	// nicht uebersetzt
	public String getRawMessage() {
		return super.getMessage();
	}
}
