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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import sipka.syntax.parser.model.ParsingCancelledException;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public abstract class Rule {
	public static final String BUILTIN_VAR_PREFIX = "@";
	private static final String REPLACE_ALIAS_VAR_NAME = BUILTIN_VAR_PREFIX + "alias_replace";
	private static final String PARAMETER_VAR_NAME = BUILTIN_VAR_PREFIX + "param";

	private static final AtomicInteger ruleCounter = new AtomicInteger();

	private final String identifierName;
	private List<Pair<String, Class<?>>> params;
	private final int ruleId;
	private DocumentRegion ruleDocumentPosition;

	public Rule(String identifierName) {
		this(identifierName, null);
	}

	public Rule(String identifierName, DocumentRegion ruledocposition) {
		this.identifierName = identifierName;
		this.ruleId = ruleCounter.getAndIncrement();
		this.ruleDocumentPosition = ruledocposition;
	}

	public DocumentRegion getRuleDocumentPosition() {
		return ruleDocumentPosition;
	}

	/* package */ void setRuleDocumentPosition(DocumentRegion ruleDocumentPosition) {
		this.ruleDocumentPosition = ruleDocumentPosition;
	}

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

		DocumentData cs = new DocumentData(s);

		final int slen = cs.length();
		ParsingResult result = parseStatementImpl(helper, cs, context, parsedata);

		if (result.isSucceeded()) {
			int count = slen - cs.length();
			s.removeFromStart(count);
		}
		return result;
	}

	public final ParsingResult repairStatement(ParseHelper helper, Statement statement, ParsingInformation parsinginfo,
			DocumentData s, ParseContext context, Predicate<? super Statement> modifiedstatementpredicate,
			ParseTimeData parsedata) {
		ParsingResult result = repairStatementImpl(helper, statement, parsinginfo, s, context,
				modifiedstatementpredicate, parsedata);
		return result;
	}

	public final void repairStatementSkipped(Statement statement, ParseContext context,
			ParsingInformation parsinginfo) {
		repairStatementSkippedImpl(statement, context, parsinginfo);
	}

	public final String getIdentifierName() {
		return identifierName;
	}

	public final int getParamsCount() {
		return params == null ? 0 : params.size();
	}

	public final boolean hasParams() {
		return params != null && !params.isEmpty();
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

	protected static String getRuleAliasVarName(Rule r) {
		return REPLACE_ALIAS_VAR_NAME + r.getRuleId();
	}

}
