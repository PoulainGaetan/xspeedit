package sample.vsct.exception;

public class TailleColisException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	String message;

	public TailleColisException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
