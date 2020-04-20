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
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.rule.ParseHelper;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.rule.Rule;
import sipka.syntax.parser.model.rule.container.value.ValueConsumer;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.CollectionParsingInformation;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public abstract class ContainerRule extends Rule {
	private final List<Pair<Rule, ParseTimeData>> children = new ArrayList<>();

	public ContainerRule(String identifierName) {
		super(identifierName);
	}

	protected abstract ParsingResult parseChildren(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata);

	protected abstract ParsingResult repairChildren(ParseHelper helper, Statement statement,
			ParsingInformation parsinginfo, DocumentData s, ParseContext context,
			Predicate<? super Statement> modifiedstatementpredicate, ParseTimeData parsedata);

	@Override
	protected final ParsingResult parseStatementImpl(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
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

	@Override
	protected void repairStatementSkippedImpl(Statement statement, ParseContext context,
			ParsingInformation parsinginfo) {
		CollectionParsingInformation collectionparsinginfo = (CollectionParsingInformation) parsinginfo;

		List<Statement> directchildren = statement.getDirectChildren();
		Iterator<Statement> childit = directchildren.iterator();
		if (childit.hasNext()) {
			Iterator<ParsingInformation> infoit = collectionparsinginfo.getChildren().iterator();
			do {
				Statement childstm = childit.next();
				ParsingInformation childinfo = infoit.next();
				childinfo.getRule().repairStatementSkipped(childstm, context, childinfo);
			} while (childit.hasNext());
		}
	}

	public final ContainerRule addChild(Pair<Rule, ParseTimeData> r) {
		getChildren().add(r);
		return this;
	}

	public final ContainerRule addChild(Rule r, ParseTimeData pdata) {
		return addChild(new Pair<>(r, pdata));
	}

	public final Collection<Pair<Rule, ParseTimeData>> getChildren() {
		return children;
	}
}
