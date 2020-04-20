package sipka.syntax.parser.model.rule;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import sipka.syntax.parser.model.occurrence.Occurrence;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.parse.params.RegexParam;
import sipka.syntax.parser.model.rule.consume.MatchesRule;
import sipka.syntax.parser.model.rule.consume.SkipRule;
import sipka.syntax.parser.model.rule.container.order.AnyOrderRule;
import sipka.syntax.parser.model.rule.container.order.FirstOrderRule;
import sipka.syntax.parser.model.rule.container.order.InOrderRule;
import sipka.syntax.parser.model.rule.container.value.ValueRule;
import sipka.syntax.parser.model.rule.invoke.InvokeRule;
import sipka.syntax.parser.util.Pair;

public class RuleFactory {
	private int ruleIdCounter = 0;

	public Occurrence occurrence(String occurrence) {
		return Occurrence.parse(occurrence);
	}

	public Pattern pattern(String pattern) {
		//TODO cache pattern
		//add \A to match exactly the beginning of the input
		//https://docs.oracle.com/javase/tutorial/essential/regex/bounds.html
		//make the group we introduce non capturing, so references will be correct
		try {
			return Pattern.compile("\\A(?:" + pattern + ")");
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to compile pattern: " + pattern, e);
		}
	}

	private <T extends Rule> T returnRule(T rule) {
		rule.ruleId = ++ruleIdCounter;
		return rule;
	}

	public InvokeRule createInvokeRule(String identifiername, InvokeParam<?> ruleParam, String alias,
			List<InvokeParam<?>> invokeParams) {
		return returnRule(new InvokeRule(identifiername, ruleParam, alias,
				invokeParams == null ? Collections.emptyList() : invokeParams));
	}

	public InvokeRule createInvokeRule(InvokeParam<?> ruleParam, String alias, List<InvokeParam<?>> invokeParams) {
		return createInvokeRule(null, ruleParam, alias, invokeParams);
	}

	public InvokeRule createInvokeRule(String identifiername, InvokeParam<?> ruleParam, String alias) {
		return createInvokeRule(identifiername, ruleParam, alias, null);
	}

	public InvokeRule createInvokeRule(InvokeParam<?> ruleParam, String alias) {
		return createInvokeRule(null, ruleParam, alias, null);
	}

	public InvokeRule createInvokeRule(InvokeParam<?> ruleParam) {
		return createInvokeRule(null, ruleParam, null, null);
	}

	public InvokeRule createInvokeRule(InvokeParam<?> ruleParam, List<InvokeParam<?>> invokeParams) {
		return createInvokeRule(null, ruleParam, null, invokeParams);
	}

	public MatchesRule createMatchesRule(Pattern pattern) {
		return createMatchesRule(new RegexParam(pattern));
	}

	public MatchesRule createMatchesRule(InvokeParam<Pattern> param) {
		return returnRule(new MatchesRule(null, param));
	}

	public MatchesRule createMatchesRule(String identifierName, InvokeParam<Pattern> param) {
		return returnRule(new MatchesRule(identifierName, param));
	}

	public MatchesRule createMatchesRule(String identifierName, Pattern pattern) {
		return createMatchesRule(identifierName, new RegexParam(pattern));
	}

	public SkipRule createSkipRule(Pattern pattern) {
		return createSkipRule(new RegexParam(pattern));
	}

	public SkipRule createSkipRule(InvokeParam<Pattern> param) {
		return returnRule(new SkipRule(null, param));
	}

	public SkipRule createSkipRule(String identifierName, InvokeParam<Pattern> param) {
		return returnRule(new SkipRule(identifierName, param));
	}

	public SkipRule createSkipRule(String identifierName, Pattern pattern) {
		return createSkipRule(identifierName, new RegexParam(pattern));
	}

	public AnyOrderRule createAnyOrderRule(String identifierName) {
		return returnRule(new AnyOrderRule(identifierName));
	}

	public AnyOrderRule createAnyOrderRule() {
		return createAnyOrderRule(null);
	}

	public InOrderRule createInOrderRule(String identifierName) {
		return returnRule(new InOrderRule(identifierName));
	}

	public InOrderRule createInOrderRule() {
		return createInOrderRule(null);
	}

	public FirstOrderRule createFirstOrderRule(String identifierName) {
		return returnRule(new FirstOrderRule(identifierName));
	}

	public FirstOrderRule createFirstOrderRule() {
		return createFirstOrderRule(null);
	}

	public ValueRule createValueRule(String valueName, Pair<Rule, ParseTimeData> rule) {
		ValueRule vr = createValueRule(valueName);
		vr.addChild(rule);
		return vr;
	}

	public ValueRule createValueRule(String valueName, Rule r, ParseTimeData pdata) {
		return createValueRule(valueName, new Pair<>(r, pdata));
	}

	public ValueRule createValueRule(String valueName) {
		return returnRule(new ValueRule(valueName));
	}
}
