package de.leonso.xfl;

public enum DataType {

	CODE_REF(1),
	CODE_VAR(2),
	CODE_BOTH(3),
	
	UNAVAILABLE(4),
	OBJECT(8),
	ITEM_REF(17),
	ITEM_VAR(34),
	
	NULL(0);
	
	int value;
	
	private DataType(int v) {
		this.value = v;
	}
	
//	public int value() {
//		return value;
//	}
	
	public DataType getMixedType(DataType d) {
		int m = value & d.value;
		
		if (m==0) {
			return NULL;
		} else if (m==1) {
			return CODE_REF;
		} else if (m==2) {
			return CODE_VAR;
		} else if (m==3) {
			return CODE_BOTH;
		}
		return NULL;
	}
}
