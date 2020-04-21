package de.leonso.xfl;

public enum ReservedWords {
	REM, FIELD, GLOBAL, DEFINE, UNDEFINE,
	ENVIRONMENT, OBJECT, LABEL, DEFAULT, ALIAS,
	ORIGINAL, CALL;
	
	public static boolean contains(String test) {

	    for (ReservedWords c : ReservedWords.values()) {
	        if (c.name().equals(test)) {
	            return true;
	        }
	    }

	    return false;
	}
}
