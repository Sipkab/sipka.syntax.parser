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
import sipka.syntax.parser.util.ArrayRangeCharSequence;

public class ValueRule extends InOrderRule {
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

	private boolean nonEmpty = false;

	public ValueRule(String valueName) {
		super(valueName);
	}

	private ParsingResult executeParsing(ParseContext context,
			Function<? super ParseContext, ? extends ParsingResult> executor) {
		ValueConsumer valueconsumer = new ValueConsumer();
		CallingContext valuecontext = new CallingContext(context, valueconsumer);

		ParsingResult parsedchildrenresult = executor.apply(valuecontext);

		ArrayRangeCharSequence parsedval = valueconsumer.getParsedValue();
		ValueParsingInformation usingparsinginfo = new ValueParsingInformation(this,
				parsedchildrenresult.getParsingInformation());
		if (!parsedchildrenresult.isSucceeded() || (nonEmpty && parsedval.length() == 0)) {
			return new ParsingResult(null, usingparsinginfo);
		}
		Statement parsedchildrenstm = parsedchildrenresult.getStatement();

		ValueStatement valuestm = new ValueStatement(getIdentifierName(), parsedval, parsedchildrenstm);
		return new ParsingResult(valuestm, usingparsinginfo);
	}

	@Override
	protected ParsingResult parseChildren(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		return executeParsing(context, valuecontext -> super.parseChildren(helper, s, valuecontext, parsedata));
	}

	@Override
	protected ParsingResult repairChildren(ParseHelper helper, Statement statement, ParsingInformation parsinginfo,
			DocumentData s, ParseContext context, Predicate<? super Statement> modifiedstatementpredicate,
			ParseTimeData parsedata) {
		ValueParsingInformation valueinfo = (ValueParsingInformation) parsinginfo;
		ValueStatement vstm = (ValueStatement) statement;

		return executeParsing(context, valuecontext -> super.repairChildren(helper, vstm.getSubStatement(),
				valueinfo.getSubInformation(), s, valuecontext, modifiedstatementpredicate, parsedata));
	}

	@Override
	protected void repairStatementSkippedImpl(Statement statement, ParseContext context,
			ParsingInformation parsinginfo) {
		//empty on purpose
		//no need to call the children, as their matched values would apply to THIS statement
		//however, as this statement is skipped during reparation, the value will be the same as
		//it is currently, therefore it is unnecessary to call the children reparation skip functions
	}

	@Override
	public String toString() {
		return "ValueRule [getIdentifierName()=" + getIdentifierName() + "]";
	}

	public void setNonEmpty(boolean nonempty) {
		this.nonEmpty = nonempty;
	}

}
