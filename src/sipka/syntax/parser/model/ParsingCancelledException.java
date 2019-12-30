package sipka.syntax.parser.model;

public class ParsingCancelledException extends FatalParseException {
	private static final String MESSAGE = "Parsing cancelled by user.";
	private static final long serialVersionUID = 1L;

	public ParsingCancelledException() {
		super(MESSAGE);
	}

}
