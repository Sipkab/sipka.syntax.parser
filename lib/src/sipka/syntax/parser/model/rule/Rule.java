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
package sipka.syntax.parser.model.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import sipka.syntax.parser.model.ParsingCancelledException;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public abstract class Rule {
	public static final String BUILTIN_VAR_PREFIX = "@";
	private static final String PARAMETER_VAR_NAME = BUILTIN_VAR_PREFIX + "param";

	private final String identifierName;
	private List<Pair<String, Class<?>>> params;
	/*default*/ int ruleId;

	public Rule(String identifierName) {
		this.identifierName = identifierName;
	}

	public abstract void accept(RuleVisitor visitor);

	protected abstract ParsingResult parseStatementImpl(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata);

	protected abstract ParsingResult repairStatementImpl(ParseHelper helper, Statement statement,
			ParsingInformation parsinginfo, DocumentData s, ParseContext context,
			Predicate<? super Statement> modifiedstatementpredicate, ParseTimeData parsedata);

	/**
	 * XXX documented to avoid unused warning.
	 * 
	 * @param statement
	 * @param context
	 * @param parsinginfo
	 */
	protected void repairStatementSkippedImpl(Statement statement, ParseContext context,
			ParsingInformation parsinginfo) {
	}

	public final ParsingResult parseStatement(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		if (helper.getProgressMonitor().isCancelled()) {
			throw new ParsingCancelledException();
		}

		final int slen = s.length();

		int offset = s.getDocumentOffset();
		ParsingResult existingresult = helper.getExistingParseResult(this, offset, context);
		if (existingresult != null) {
			if (existingresult.isSucceeded()) {
				repairAndAdjustDocument(this, existingresult.getStatement(), context,
						existingresult.getParsingInformation(), s);
			}
			return existingresult;
		}

		DocumentData cs = new DocumentData(s);

		ParsingResult result = parseStatementImpl(helper, cs, context, parsedata);

		if (result.isSucceeded()) {
			int count = slen - cs.length();
			s.removeFromStart(count);
		}
		helper.ruleParsed(this, result, offset, context);
		return result;
	}

	public final ParsingResult repairStatement(ParseHelper helper, Statement statement, ParsingInformation parsinginfo,
			DocumentData s, ParseContext context, Predicate<? super Statement> modifiedstatementpredicate,
			ParseTimeData parsedata) {
		int offset = s.getDocumentOffset();
		ParsingResult existingresult = helper.getExistingParseResult(this, offset, context);
		if (existingresult != null) {
			if (existingresult.isSucceeded()) {
				repairAndAdjustDocument(this, existingresult.getStatement(), context,
						existingresult.getParsingInformation(), s);
			}
			return existingresult;
		}

		ParsingResult result = repairStatementImpl(helper, statement, parsinginfo, s, context,
				modifiedstatementpredicate, parsedata);

		helper.ruleParsed(this, result, offset, context);
		return result;
	}

	public final void repairStatementSkipped(Statement statement, ParseContext context,
			ParsingInformation parsinginfo) {
		repairStatementSkippedImpl(statement, context, parsinginfo);
	}

	protected static final void repairAndAdjustDocument(Rule rule, Statement statement, ParseContext context,
			ParsingInformation parsinginfo, DocumentData s) {
		rule.repairStatementSkipped(statement, context, parsinginfo);
		s.removeFromStart(statement.getLength());
	}

	public final String getIdentifierName() {
		return identifierName;
	}

	public final List<Pair<String, Class<?>>> getDeclaredParams() {
		if (params == null) {
			params = new ArrayList<>();
		}
		return params;
	}

	public final int getRuleId() {
		return ruleId;
	}

	@Override
	public int hashCode() {
		return ruleId;
	}

	@Override
	public boolean equals(Object obj) {
		//identity equality for performance
		//we only expect rules from the same language to be compared, 
		//therefore we don't need to parform deeper equality checks
		return this == obj;
	}

	public String createParameterName(String name) {
		return PARAMETER_VAR_NAME + "_" + getRuleId() + "_" + name;
	}

}
