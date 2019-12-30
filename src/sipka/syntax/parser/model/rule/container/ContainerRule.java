package sipka.syntax.parser.model.rule.container;

import java.util.ArrayList;
import java.util.Collection;

import sipka.syntax.parser.model.FatalParseException;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.rule.ParseHelper;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.rule.Rule;
import sipka.syntax.parser.util.Pair;

public abstract class ContainerRule extends Rule {
	private final Collection<Pair<Rule, ParseTimeData>> children = new ArrayList<>();
	private boolean defined;

	public ContainerRule() {
		super(null);
	}

	public ContainerRule(String identifierName) {
		super(identifierName);
	}

	public void setDefined() {
		this.defined = true;
	}

	public boolean isDefined() {
		return defined;
	}

	protected abstract ParsingResult parseChildren(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata);

	@Override
	protected final ParsingResult parseStatementImpl(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		if (!isDefined()) {
			throw new FatalParseException("Container rule with ID: " + getIdentifierName() + " was not defined.");
		}
		return parseChildren(helper, s, context, parsedata);
	}

	@SafeVarargs
	public final ContainerRule addChild(Pair<Rule, ParseTimeData>... r) {
		if (r.length > 0) {
			setDefined();
		}
		for (Pair<Rule, ParseTimeData> pair : r) {
			getChildren().add(new Pair<>(pair.key, pair.value));
		}
		return this;
	}

	public final ContainerRule addChild(Rule r, ParseTimeData pdata) {
		setDefined();
		return addChild(new Pair<>(r, pdata));
	}

	protected Collection<Pair<Rule, ParseTimeData>> getChildren() {
		return children;
	}
}
