package ch.ethz.sae;

public class ConversionArgumentException extends IllegalArgumentException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConversionArgumentException() {
		super();
	}
	
	public ConversionArgumentException(String message) {
		super(message);
	}
	
	public ConversionArgumentException(Throwable cause) {
		super(cause);
	}
	
	public ConversionArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

}
