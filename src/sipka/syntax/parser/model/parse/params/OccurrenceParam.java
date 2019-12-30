package sipka.syntax.parser.model.parse.params;

import sipka.syntax.parser.model.ParseFailedException;
import sipka.syntax.parser.model.occurrence.Occurrence;
import sipka.syntax.parser.model.parse.context.ParseContext;

public class OccurrenceParam implements InvokeParam<Occurrence> {
	private final Occurrence occurrence;

	public OccurrenceParam(String occurrenceString) throws ParseFailedException {
		this.occurrence = Occurrence.parse(occurrenceString);
	}

	public OccurrenceParam(Occurrence occurrence) {
		this.occurrence = occurrence;
	}

	@Override
	public Occurrence getValue(ParseContext context) {
		return occurrence;
	}

	@Override
	public String toString() {
		return "OccurrenceParam [occurrence=" + occurrence + "]";
	}

}
