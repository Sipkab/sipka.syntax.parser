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
package sipka.syntax.parser.model.rule.consume;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.rule.RuleVisitor;
import sipka.syntax.parser.model.rule.container.value.ValueConsumer;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.ArrayRangeCharSequence;

public class MatchesRule extends ConsumeRule {
	//TODO complete implementation of replacement
	private String replacement = null;

	public MatchesRule(String identifierName, InvokeParam<Pattern> param) {
		super(identifierName, param);
	}

	@Override
	public void accept(RuleVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected void charactersConsumed(Matcher matcher, ArrayRangeCharSequence parsed, ParseContext context) {
		ValueConsumer targetValue = context.getCurrentValueConsumer();
		if (targetValue != null) {
			if (replacement == null) {
				targetValue.appendValue(parsed);
			} else {
				//TODO use StringBuilder in case of jdk 9+
				StringBuffer sb = new StringBuffer();
				matcher.appendReplacement(sb, replacement);
				//TODO appendTail?
				targetValue.appendValue(sb);
			}
		}
	}

	@Override
	protected void repairStatementSkippedImpl(Statement statement, ParseContext context,
			ParsingInformation parsinginfo) {
		ValueConsumer targetValue = context.getCurrentValueConsumer();
		if (targetValue != null) {
			targetValue.appendValue(statement.getValueSequence());
		}
	}
}
