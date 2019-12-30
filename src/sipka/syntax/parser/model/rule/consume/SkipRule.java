package sipka.syntax.parser.model.rule.consume;

import java.util.regex.Pattern;

import sipka.syntax.parser.model.parse.params.InvokeParam;

public class SkipRule extends ConsumeRule {
	public SkipRule(Pattern pattern) {
		super(pattern);
	}

	public SkipRule(InvokeParam<Pattern> param) {
		super(param);
	}

	public SkipRule(String identifierName, InvokeParam<Pattern> param) {
		super(identifierName, param);
	}

	public SkipRule(String identifierName, Pattern pattern) {
		super(identifierName, pattern);
	}

}
