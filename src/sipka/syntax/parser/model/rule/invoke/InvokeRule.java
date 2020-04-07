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
package sipka.syntax.parser.model.rule.invoke;

import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import sipka.syntax.parser.model.FatalParseException;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.CallingContext;
import sipka.syntax.parser.model.parse.context.DeclaringContext;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.rule.ParseHelper;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.rule.Rule;
import sipka.syntax.parser.model.rule.RuleDeclaration;
import sipka.syntax.parser.model.statement.CollectionStatement;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.ValueStatement;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public class InvokeRule extends Rule {
	protected static class InvokeParsingInformation extends ParsingInformation {
		private ParsingInformation subInformation;

		public InvokeParsingInformation(Rule rule, ParsingInformation subInformation) {
			super(rule, new DocumentRegion(subInformation.getRegionOfInterest()));
			this.subInformation = subInformation;
		}

		@Override
		public ParsingInformation clone() {
			InvokeParsingInformation result = (InvokeParsingInformation) super.clone();
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

	private final InvokeParam<?> ruleParam;
	private final String alias;
	private final List<InvokeParam<?>> invokeParams;

	public InvokeRule(InvokeParam<?> ruleParam, String alias) {
		this(ruleParam, alias, Collections.emptyList());
	}

	public InvokeRule(InvokeParam<?> ruleParam, String alias, List<InvokeParam<?>> invokeParams) {
		super(null);
		this.ruleParam = ruleParam;
		this.alias = alias;
		this.invokeParams = invokeParams;
	}

	private interface InvokeExecutor {
		public ParsingResult execute(Rule rule, ParseContext context, ParseTimeData parsetimedata);
	}

	private ParsingResult executeParsing(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata, InvokeExecutor executor) {
		Object ruleval = ruleParam.getValue(helper, context);
		if (ruleval == null) {
			throw new FatalParseException("Rule not found: " + ruleParam + ".");
		}
		if (!(ruleval instanceof RuleDeclaration)) {
			throw new FatalParseException("Referenced object is not a rule: " + ruleParam + ".");
		}
		RuleDeclaration invokedruledecl = (RuleDeclaration) ruleval;
		DeclaringContext invokedruledeclcontext = invokedruledecl.getDeclarationContext();
		Rule invokerule = invokedruledecl.getRule();
		if (invokedruledeclcontext == null) {
			throw new FatalParseException("Referenced rule was not defined: " + invokerule + ".");
		}

		List<Pair<String, Class<?>>> declaredparams = invokerule.getDeclaredParams();

		int ipsize = invokeParams.size();
		int dpsize = declaredparams.size();
		if (dpsize != ipsize) {
			throw new IllegalArgumentException(
					"Included rule parameter count doesnt match expected: " + dpsize + " got: " + ipsize);
		}

		NavigableMap<String, Object> varmap;
		if (ipsize == 0) {
			varmap = Collections.emptyNavigableMap();
		} else {
			varmap = new TreeMap<>();
			for (int i = 0; i < ipsize; i++) {
				InvokeParam<?> param = invokeParams.get(i);
				Pair<String, Class<?>> targetParam = declaredparams.get(i);

				Object value = param.getValue(helper, context);

				if (!targetParam.value.isInstance(value)) {
					throw new IllegalArgumentException("Included parameter cannot be converted from: "
							+ value.getClass().getName() + " to: " + targetParam.value.getName());
				}

				varmap.put(invokerule.createParameterName(targetParam.key), value);
			}
		}

		ParseContext invokecontext = CallingContext.mergeWithBuiltInsAndVariables(context, invokedruledeclcontext,
				varmap);

		ParseTimeData invokeparsetimedata = new ParseTimeData(parsedata.getOccurrence(helper, context),
				invokedruledeclcontext);
		ParsingResult result = executor.execute(invokerule, invokecontext, invokeparsetimedata);
		InvokeParsingInformation usingparsinginfo = new InvokeParsingInformation(this, result.getParsingInformation());
		if (!result.isSucceeded()) {
			return new ParsingResult(null, usingparsinginfo);
		}
		Statement resultstm = result.getStatement();
		if (alias != null && resultstm instanceof ValueStatement) {
			resultstm = ((ValueStatement) resultstm).withName(alias);
		}
		return new ParsingResult(new CollectionStatement(s.subDocumentSequence(resultstm.getPosition()),
				resultstm.getPosition(), Collections.singletonList(resultstm)), usingparsinginfo);
	}

	@Override
	protected ParsingResult parseStatementImpl(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		return executeParsing(helper, s, context, parsedata, (invokedrule, invokeContext, ruleparsedata) -> invokedrule
				.parseStatement(helper, s, invokeContext, ruleparsedata));
//		Rule invokedrule = ruleParam.getValue(context);
//		if (invokedrule == null) {
//			throw new FatalParseException("Rule not found: " + ruleParam + ".");
//		}
//
//		CallingContext invokeContext = new CallingContext(parsedata.getDeclaringContext());
//		invokeContext.putAllBuiltInFrom(context);
//		invokeContext.putObject(getRuleAliasVarName(invokedrule), alias);
//		List<Pair<String, Class<?>>> declaredparams = invokedrule.getDeclaredParams();
//
//		if (declaredparams.size() != invokeParams.size()) {
//			throw new IllegalArgumentException(
//					"Included rule parameter count doesnt match expected: " + declaredparams.size() + " got: " + invokeParams.size());
//		}
//
//		for (int i = 0; i < invokeParams.size(); i++) {
//			InvokeParam<?> param = invokeParams.get(i);
//			Pair<String, Class<?>> targetParam = declaredparams.get(i);
//
//			Object value = param.getValue(context);
//
//			if (!targetParam.value.isInstance(value)) {
//				throw new IllegalArgumentException(
//						"Included parameter cannot be converted from: " + value.getClass().getName() + " to: " + targetParam.value.getName());
//			}
//
//			invokeContext.putObject(invokedrule.createParameterName(targetParam.key), value);
//		}
//
//		ParsingResult result = invokedrule.parseStatement(helper, s, invokeContext, parsedata);
//		InvokeParsingInformation usingparsinginfo = new InvokeParsingInformation(this, result.getParsingInformation());
//		if (!result.isSucceeded()) {
//			return new ParsingResult(null, usingparsinginfo);
//		}
//		Statement resultstm = result.getStatement();
//		return new ParsingResult(
//				new CollectionStatement(s.subDocumentSequence(resultstm.getPosition()), resultstm.getPosition(), Collections.singletonList(resultstm)),
//				usingparsinginfo);
	}

	@Override
	protected ParsingResult repairStatementImpl(ParseHelper helper, Statement statement, ParsingInformation parsinginfo,
			DocumentData s, ParseContext context, Predicate<? super Statement> modifiedstatementpredicate,
			ParseTimeData parsedata) {
		InvokeParsingInformation invokeinfo = (InvokeParsingInformation) parsinginfo;

		return executeParsing(helper, s, context, parsedata,
				(invokedrule, invokeContext, ruleparsedata) -> invokedrule.repairStatement(helper,
						statement.getDirectChildren().get(0), invokeinfo.getSubInformation(), s, invokeContext,
						modifiedstatementpredicate, ruleparsedata));
//		Rule invokedrule = ruleParam.getValue(context);
//		if (invokedrule == null) {
//			throw new FatalParseException("Rule not found: " + ruleParam + ".");
//		}
//		CallingContext invokecontext = new CallingContext(parsedata.getDeclaringContext());
//		invokecontext.putAllBuiltInFrom(context);
//		invokecontext.putObject(getRuleAliasVarName(invokedrule), alias);
//
//		List<Pair<String, Class<?>>> declaredparams = invokedrule.getDeclaredParams();
//		for (int i = 0; i < invokeParams.size(); i++) {
//			InvokeParam<?> param = invokeParams.get(i);
//			Pair<String, Class<?>> targetParam = declaredparams.get(i);
//
//			Object value = param.getValue(context);
//
//			if (!targetParam.value.isInstance(value)) {
//				throw new IllegalArgumentException(
//						"Included parameter cannot be converted from: " + value.getClass().getName() + " to: " + targetParam.value.getName());
//			}
//
//			invokecontext.putObject(invokedrule.createParameterName(targetParam.key), value);
//		}
//
//		ParsingResult result = invokedrule.repairStatement(statement.getChildren().get(0), invokeinfo.getSubInformation(), s, invokecontext,
//				modifiedstatementpredicate, parsedata);
//		InvokeParsingInformation usingparsinginfo = new InvokeParsingInformation(this, result.getParsingInformation());
//		if (!result.isSucceeded()) {
//			return new ParsingResult(null, usingparsinginfo);
//		}
//		Statement resultstm = result.getStatement();
//		return new ParsingResult(
//				new CollectionStatement(s.subDocumentSequence(resultstm.getPosition()), resultstm.getPosition(), Collections.singletonList(resultstm)),
//				usingparsinginfo);
	}

	@Override
	public String toString() {
		return "InvokeRule [" + (ruleParam != null ? "ruleParam=" + ruleParam + ", " : "")
				+ (invokeParams.size() > 0 ? "invokeParams=" + invokeParams + ", " : "")
				+ (alias != null ? "alias=" + alias : "") + "]";
	}
}
