package de.leonso.xfl;

import java.io.Serializable;

/**
 * Verarbeitung von Systemmeldungen
 * @see XflEngine#setLogger(ILogger)
 */
public interface ILogger extends Serializable {

	void log(String string);

}
