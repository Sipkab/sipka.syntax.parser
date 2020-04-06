/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sipka.syntax.parser.model.rule.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import sipka.syntax.parser.model.FatalParseException;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.rule.ParseHelper;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.rule.Rule;
import sipka.syntax.parser.model.rule.container.value.ValueRule.ValueConsumer;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public abstract class ContainerRule extends Rule {
	private final Collection<Pair<Rule, ParseTimeData>> children = new ArrayList<>();
	@Deprecated
	private boolean defined;

	public ContainerRule() {
		super(null);
	}

	public ContainerRule(String identifierName) {
		super(identifierName);
	}

	@Deprecated
	public void setDefined() {
		this.defined = true;
	}

	@Deprecated
	public boolean isDefined() {
		return defined;
	}

	protected abstract ParsingResult parseChildren(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata);

	protected abstract ParsingResult repairChildren(ParseHelper helper, Statement statement,
			ParsingInformation parsinginfo, DocumentData s, ParseContext context,
			Predicate<? super Statement> modifiedstatementpredicate, ParseTimeData parsedata);

	@Override
	protected final ParsingResult parseStatementImpl(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		if (!isDefined()) {
			throw new FatalParseException("Container rule with ID: " + getIdentifierName() + " was not defined at: "
					+ getRuleDocumentPosition());
		}
		ValueConsumer valconsumer = context.getCurrentValueConsumer();
		int vclen = 0;
		if (valconsumer != null) {
			vclen = valconsumer.length();
		}
		ParsingResult result = parseChildren(helper, s, context, parsedata);
		if (!result.isSucceeded()) {
			if (valconsumer != null) {
				valconsumer.setLength(vclen);
			}
		}
		return result;
	}

	@Override
	protected final ParsingResult repairStatementImpl(ParseHelper helper, Statement statement,
			ParsingInformation parsinginfo, DocumentData s, ParseContext context,
			Predicate<? super Statement> modifiedstatementpredicate, ParseTimeData parsedata) {
		ValueConsumer valconsumer = context.getCurrentValueConsumer();
		int vclen = 0;
		if (valconsumer != null) {
			vclen = valconsumer.length();
		}
		ParsingResult result = repairChildren(helper, statement, parsinginfo, s, context, modifiedstatementpredicate,
				parsedata);
		if (!result.isSucceeded()) {
			if (valconsumer != null) {
				valconsumer.setLength(vclen);
			}
		}
		return result;
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
