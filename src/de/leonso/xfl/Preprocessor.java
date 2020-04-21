package de.leonso.xfl;

import java.io.Serializable;

public interface Preprocessor extends Serializable {

	public String preprocess(String code);

}
