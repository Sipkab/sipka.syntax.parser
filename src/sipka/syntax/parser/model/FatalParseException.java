package sipka.syntax.parser.model;

public class FatalParseException extends RuntimeException {

	private static final long serialVersionUID = 6045533358638254098L;

	public FatalParseException() {
		super();
	}

	public FatalParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FatalParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public FatalParseException(String message) {
		super(message);
	}

	public FatalParseException(Throwable cause) {
		super(cause);
	}

}
