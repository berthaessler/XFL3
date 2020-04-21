package de.leonso.xfl;

public enum KeyWords {
	OK(1), YESNO(2), OKCANCELLIST(4), YESNOCANCEL(11), OKCANCELEDIT(3), OKCANCELCOMBO(5), OKCANCELEDITCOMBO(6), OKCANCELLISTMULT(7), CHOOSEDATABASE(13), LOCALBROWSE(12),
	// richtige Nummer, nicht im Designer dokumentiert
	// The constant name PROMPT_CHOOSEDATABASE is not implemented in Release 6.0 but the literal value 13 can be used in itstead.)

	PASSWORD(10), NOSORT(15), SINGLE(31), CUSTOM(38), ROOM(32), NAME(39), RESOURCE(33), FOLDERS(34), SHARED(35), PRIVATE(36), NODESKTOP(37),
	AUTOHORZFIT(40), AUTOVERTFIT(41), NOCANCEL(42), NONEWFIELDS(43), NOFIELDUPDATE(44), READONLY(45), NOOKCANCEL(46), SIZETOTABLE(47),
	REM(20), FIELD(21), GLOBAL(22), DEFINE(23), UNDEFINE(24), ENVIRONMENT(25), OBJECT(26), LABEL(27), DEFAULT(28), ALIAS(29), ORIGINAL(30), CALL(48);
	
	int value;
	private KeyWords(int v) {
		this.value = v;
	}
	
	public static boolean contains(String test) {

	    for (ReservedWords c : ReservedWords.values()) {
	        if (c.name().equals(test)) {
	            return true;
	        }
	    }

	    return false;
	}
	
}
