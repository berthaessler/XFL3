package de.leonso.xfl.exceptions;

import de.leonso.xfl.XflEngine;

public class ConversionException extends XflException {
	private static final long serialVersionUID = 1L;

	private Object data;
	public ConversionException(XflEngine engine, Object v) {
		super(engine, "");
		this.data = v;
	}

	@Override
	public String getMessage() {
		return getEngine().getMessage("data.cannot.be.converted", data.toString());
	}
	
}
