package sipka.syntax.parser.model.parse.params;

import sipka.syntax.parser.model.parse.context.ParseContext;

public interface InvokeParam<T> {
	public T getValue(ParseContext context);
}
