package sipka.syntax.parser.model.rule.container.value;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.CallingContext;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.rule.ParseHelper;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.rule.Rule;
import sipka.syntax.parser.model.rule.container.order.InOrderRule;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.ValueStatement;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public class ValueRule extends InOrderRule {
	public static final String TARGET_VALUE_VAR_NAME = BUILTIN_VAR_PREFIX + "target_value";

	protected static class ValueParsingInformation extends ParsingInformation {
		private ParsingInformation subInformation;

		public ValueParsingInformation(Rule rule, ParsingInformation subInformation) {
			super(rule, new DocumentRegion(subInformation.getRegionOfInterest()));
			this.subInformation = subInformation;
		}

		@Override
		public ParsingInformation clone() {
			ValueParsingInformation result = (ValueParsingInformation) super.clone();
			result.subInformation = this.subInformation.clone();
			return result;
		}

		public ParsingInformation getSubInformation() {
			return subInformation;
		}

		@Override
		public List<ParsingInformation> getChildren() {
			return Collections.singletonList(subInformation);
		}
	}

	public static class ValueConsumer {
		private StringBuilder valueBuilder = new StringBuilder();

		public void addValue(CharSequence parsed) {
			valueBuilder.append(parsed);
		}

		private CharSequence getParsedValue() {
			return valueBuilder;
		}

		@Override
		public String toString() {
			return "ValueConsumer [builder=" + valueBuilder + "]";
		}
	}

	private boolean nonEmpty = false;

	public ValueRule(String valueName, Pair<Rule, ParseTimeData> rule) {
		super(valueName);
		addChild(rule);
	}

	public ValueRule(String valueName, Rule r, ParseTimeData pdata) {
		super(valueName);
		addChild(r, pdata);
	}

	public ValueRule(String valueName) {
		super(valueName);
	}

	private ParsingResult executeParsing(ParseContext context, Function<ParseContext, ParsingResult> executor) {
		final String alias = (String) context.getObjectForName(getRuleAliasVarName(this), getIdentifierName());

		CallingContext valuecontext = new CallingContext(context);
		ValueConsumer valueconsumer = new ValueConsumer();

		valuecontext.putObject(ValueRule.TARGET_VALUE_VAR_NAME, valueconsumer);

		ParsingResult parsedchildrenresult = executor.apply(valuecontext);

		CharSequence parsedval = valueconsumer.getParsedValue();
		ValueParsingInformation usingparsinginfo = new ValueParsingInformation(this,
				parsedchildrenresult.getParsingInformation());
		if (!parsedchildrenresult.isSucceeded() || (nonEmpty && parsedval.length() == 0)) {
			return new ParsingResult(null, usingparsinginfo);
		}
		Statement parsedchildrenstm = parsedchildrenresult.getStatement();

		ValueStatement valuestm = new ValueStatement(alias, parsedval.toString(), parsedchildrenstm);
		return new ParsingResult(valuestm, usingparsinginfo);
	}

	@Override
	protected ParsingResult parseChildren(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		return executeParsing(context, valuecontext -> super.parseChildren(helper, s, valuecontext, parsedata));
	}

	@Override
	protected ParsingResult repairStatementImpl(Statement statement, ParsingInformation parsinginfo, DocumentData s,
			ParseContext context, Predicate<? super Statement> modifiedstatementpredicate, ParseTimeData parsedata) {
		ValueParsingInformation valueinfo = (ValueParsingInformation) parsinginfo;
		ValueStatement vstm = (ValueStatement) statement;

		return executeParsing(context, valuecontext -> super.repairStatementImpl(vstm.getSubStatement(),
				valueinfo.getSubInformation(), s, valuecontext, modifiedstatementpredicate, parsedata));
	}

	@Override
	public String toString() {
		return "ValueRule [getIdentifierName()=" + getIdentifierName() + "]";
	}

	public void setNonEmpty(boolean nonempty) {
		this.nonEmpty = nonempty;
	}

}
