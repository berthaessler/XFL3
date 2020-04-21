package de.leonso.xfl;

/**
 * ein Element aus dem Quellcode
 * Beim Parsen wird zunaechst der Quellcode in seine Einzelteile zerlegt.
 * Dabei werden die Token schon klassifiziert (type)
 * startPos und endPos geben die Position im Quellcode an
 * 
 * @author Bert
 *
 */
public class Token {

	private String word = null;
	private ParseType type;
	private int startPos = 0;
	private int endPos = 0;
	private Token previousToken = null;
	private Token nextToken = null;
	
	public Token(Token previous, String word, ParseType type, int startPos, int endPos) {
		this.word = word;
		this.type = type;
		this.startPos = startPos;
		this.endPos = endPos;
		if (previous != null) {
			previousToken = previous;
			previous.setNextToken(this);
		}
	}

	public String getWord() {
		return word;
	}

	public ParseType getType() {
		return type;
	}

	public int getStartPos() {
		return startPos;
	}

	public int getEndPos() {
		return endPos;
	}

	public Token getPreviousToken() {
		return previousToken;
	}

	public void setPreviousToken(Token previousToken) {
		this.previousToken = previousToken;
	}

	public Token getNextToken() {
		return nextToken;
	}

	public void setNextToken(Token nextToken) {
		this.nextToken = nextToken;
	}

	@Override
	public String toString() {
		return word;
	}
}
