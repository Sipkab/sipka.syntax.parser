package sipka.syntax.parser.model.parse.params;

import java.util.regex.Pattern;

import sipka.syntax.parser.model.parse.context.ParseContext;

public class RegexParam implements InvokeParam<Pattern> {
	private final Pattern pattern;

	public RegexParam(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public Pattern getValue(ParseContext context) {
		return pattern;
	}

	@Override
	public String toString() {
		return "RegexParam [pattern=" + pattern + "]";
	}

}
