package de.leonso.xfl;

public enum SubType {

	NULL(0),
	
	// niedriger Wert bedeutet hohe Bindungsstärke:
	OPERATOR_ADD(57),OPERATOR_MULT(56),OPERATOR_LIST(55),OPERATOR_COMP(58),OPERATOR_LOG(59),
	OPERATOR_DOT(53),
	// Objektbezeichner bindet am stärksten
	ASSIGNMENT(60), // x := 1*2
	OPERATOR_INDEX(51), // abc[2]
	OPERATOR_SEARCH(52),
	OPERATOR_UNKNOWN(53),
	
	STRING_START(31),STRING(32),FUNCTION(35),VAR(36),NUMBER(38);
	
	
	private int value;
	
	private SubType(int i) {
		this.value = i;
	}
	
	public boolean bindsStrongerThan(SubType o) {
		return o.value > this.value;
		
	}
}
