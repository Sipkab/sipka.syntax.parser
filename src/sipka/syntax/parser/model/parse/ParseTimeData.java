package sipka.syntax.parser.model.parse;

import sipka.syntax.parser.model.occurrence.Occurrence;
import sipka.syntax.parser.model.parse.context.DeclaringContext;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.parse.params.OccurrenceParam;

public final class ParseTimeData {
	private final InvokeParam<Occurrence> occurrenceParam;
	private final DeclaringContext declaringContext;

	public ParseTimeData(Occurrence occurrence, DeclaringContext declaringContext) {
		this.declaringContext = declaringContext;
		this.occurrenceParam = new OccurrenceParam(occurrence);
	}

	public ParseTimeData(InvokeParam<Occurrence> occurrence, DeclaringContext declaringContext) {
		this.declaringContext = declaringContext;
		this.occurrenceParam = occurrence;
	}

	public final Occurrence getOccurrence(ParseContext context) {
		return occurrenceParam.getValue(context);
	}

	public final DeclaringContext getDeclaringContext() {
		return declaringContext;
	}

	@Override
	public String toString() {
		return "ParseTimeData [" + (occurrenceParam != null ? "occurrenceParam=" + occurrenceParam : "") + "]";
	}

}