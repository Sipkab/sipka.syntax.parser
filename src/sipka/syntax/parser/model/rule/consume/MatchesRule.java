package sipka.syntax.parser.model.rule.consume;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.rule.container.value.ValueRule;
import sipka.syntax.parser.model.rule.container.value.ValueRule.ValueConsumer;
import sipka.syntax.parser.model.statement.Statement;

public class MatchesRule extends ConsumeRule {
	//TODO complete implementation of replacement
	private String replacement = null;

	public MatchesRule(Pattern pattern) {
		super(pattern);
	}

	public MatchesRule(InvokeParam<Pattern> param) {
		super(param);
	}

	public MatchesRule(String identifierName, InvokeParam<Pattern> param) {
		super(identifierName, param);
	}

	public MatchesRule(String identifierName, Pattern pattern) {
		super(identifierName, pattern);
	}

	@Override
	protected void charactersConsumed(Matcher matcher, CharSequence parsed, ParseContext context) {
		ValueConsumer targetValue = (ValueConsumer) context.getObjectForName(ValueRule.TARGET_VALUE_VAR_NAME);
		if (targetValue != null) {
			if (replacement == null) {
				targetValue.addValue(parsed);
			} else {
				//TODO use StringBuilder in case of jdk 9+
				StringBuffer sb = new StringBuffer();
				matcher.appendReplacement(sb, replacement);
				targetValue.addValue(sb.toString());
			}
		}
	}

	@Override
	protected void repairStatementSkippedImpl(Statement statement, ParseContext context) {
		ValueConsumer targetValue = (ValueConsumer) context.getObjectForName(ValueRule.TARGET_VALUE_VAR_NAME);
		if (targetValue != null) {
			targetValue.addValue(statement.getValue());
		}
	}
}
