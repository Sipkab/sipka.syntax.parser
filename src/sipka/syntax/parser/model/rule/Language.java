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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import sipka.syntax.parser.model.FatalParseException;
import sipka.syntax.parser.model.ParseFailedException;
import sipka.syntax.parser.model.occurrence.Occurrence;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.DeclaringContext;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.parse.params.OccurrenceParam;
import sipka.syntax.parser.model.parse.params.RegexParam;
import sipka.syntax.parser.model.parse.params.VarReferenceParam;
import sipka.syntax.parser.model.rule.consume.ConsumeRule;
import sipka.syntax.parser.model.rule.consume.MatchesRule;
import sipka.syntax.parser.model.rule.consume.SkipRule;
import sipka.syntax.parser.model.rule.container.ContainerRule;
import sipka.syntax.parser.model.rule.container.order.AnyOrderRule;
import sipka.syntax.parser.model.rule.container.order.FirstOrderRule;
import sipka.syntax.parser.model.rule.container.order.InOrderRule;
import sipka.syntax.parser.model.rule.container.value.ValueRule;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.util.Pair;

public class Language {
	public static interface ParseProgressMonitor {
		public static final ParseProgressMonitor NULLMONITOR = () -> false;

		public boolean isCancelled();
	}

	public static final Language DESCRIBER_LANGUAGE;

	public static ParseTimeData PARSE_ONCE() {
		return new ParseTimeData(Occurrence.ONCE, DeclaringContext.EMPTY);
	}

	public static ParseTimeData PARSE_ANY() {
		return new ParseTimeData(Occurrence.ANY, DeclaringContext.EMPTY);
	}

	public static ParseTimeData PARSE_OPTIONAL() {
		return new ParseTimeData(Occurrence.OPTIONAL, DeclaringContext.EMPTY);
	}

	public static ParseTimeData PARSE_MIN_ONCE() {
		return new ParseTimeData(Occurrence.MIN_ONCE, DeclaringContext.EMPTY);
	}

	public static ParseTimeData PARSE(Occurrence occurr) {
		return new ParseTimeData(occurr, DeclaringContext.EMPTY);
	}

	static {
		RuleFactory factory = new RuleFactory();
		final Pattern WHITESPACE_PATTERN = factory.pattern("[ \\t\\r\\n\\v\\f]+");
		final SkipRule WHITESPACE = factory.createSkipRule(WHITESPACE_PATTERN);

		final ConsumeRule regexmatch = factory.createMatchesRule(factory.pattern("\"((\\\\\")|(\\\\\\\\)|[^\"])*\""));
		final ConsumeRule bracketopen = factory.createSkipRule(factory.pattern("\\{"));
		final ConsumeRule bracketclose = factory.createSkipRule(factory.pattern("\\}"));
		final ConsumeRule occurrencematch = factory.createMatchesRule(factory.pattern(
				"(([0-9]+[+-]?)|\\*|\\?|\\+)+([ \\t\\r\\n\\v\\f]*\\|[ \\t\\r\\n\\v\\f]*(([0-9]+[+-]?)|\\*|\\?|\\+))*"));
		final ConsumeRule declarednamematch = factory.createMatchesRule(factory.pattern("[a-zA-Z_][a-zA-Z0-9_]*"));
		final ConsumeRule semicolonskip = factory.createSkipRule(factory.pattern(";"));

		final ContainerRule languagenode = factory.createValueRule("language_node");
		final ContainerRule consumenode = factory.createValueRule("consume_node");
		final ContainerRule containernode = factory.createValueRule("container_node");
		final ContainerRule declarenode = factory.createValueRule("declare_node");
		final ContainerRule invokenode = factory.createValueRule("invoke_node");
		final ContainerRule forwarddeclarenode = factory.createValueRule("forwarddeclare_container_node");

		final ContainerRule generaldeclarations = factory.createAnyOrderRule();

		final ContainerRule ordernodebody = factory.createValueRule("body")//
				.addChild(new Pair<>(bracketopen, PARSE_ONCE()))//
				.addChild(new Pair<>(generaldeclarations, PARSE_ONCE()))//
				.addChild(new Pair<>(bracketclose, PARSE_ONCE()));
		final ContainerRule occurrencevalue = factory.createFirstOrderRule()//
				.addChild(factory.createValueRule("occurrence", occurrencematch, PARSE_ONCE()), PARSE_ONCE())//
				.addChild(factory.createValueRule("occurrence_ref", declarednamematch, PARSE_ONCE()), PARSE_ONCE());

		declarenode.addChild(factory.createValueRule("type",
				factory.createMatchesRule(factory.pattern("regex|occurrence")), PARSE_ONCE()), PARSE_ONCE());
		declarenode.addChild(WHITESPACE, PARSE_MIN_ONCE());
		declarenode.addChild(factory.createValueRule("name", declarednamematch, PARSE_ONCE()), PARSE_ONCE());
		declarenode.addChild(WHITESPACE, PARSE_MIN_ONCE());
		declarenode.addChild(factory.createFirstOrderRule()//
				.addChild(factory.createValueRule("expression", regexmatch, PARSE_ONCE()), PARSE_ONCE())//
				.addChild(factory.createValueRule("expression", occurrencematch, PARSE_ONCE()), PARSE_ONCE())//
				, PARSE_ONCE());
		declarenode.addChild(WHITESPACE, PARSE_ANY());
		declarenode.addChild(semicolonskip, PARSE_ONCE());

		forwarddeclarenode.addChild(
				factory.createValueRule("type",
						factory.createMatchesRule(factory.pattern("value|inorder|anyorder|firstorder")), PARSE_ONCE()),
				PARSE_ONCE());
		forwarddeclarenode.addChild(WHITESPACE, PARSE_MIN_ONCE());
		forwarddeclarenode.addChild(factory.createValueRule("name", declarednamematch, PARSE_ONCE()), PARSE_ONCE());
		forwarddeclarenode.addChild(WHITESPACE, PARSE_ANY());
		forwarddeclarenode.addChild(semicolonskip, PARSE_ONCE());

		consumenode.addChild(factory.createValueRule("type", factory.createMatchesRule(factory.pattern("matches|skip")),
				PARSE_ONCE()), PARSE_ONCE());
		consumenode.addChild(new Pair<>(WHITESPACE, PARSE_MIN_ONCE()));
		consumenode.addChild(factory.createValueRule("alias_name")//
				.addChild(factory.createSkipRule(factory.pattern("as")), PARSE_ONCE())//
				.addChild(new Pair<>(WHITESPACE, PARSE_MIN_ONCE()))//
				.addChild(declarednamematch, PARSE_ONCE())//
				.addChild(new Pair<>(WHITESPACE, PARSE_MIN_ONCE()))//
				, PARSE_OPTIONAL());
		consumenode.addChild(factory.createFirstOrderRule()//
				.addChild(factory.createValueRule("regex", regexmatch, PARSE_ONCE()), PARSE_ONCE())//
				.addChild(factory.createValueRule("regex_ref", declarednamematch, PARSE_ONCE()), PARSE_ONCE())//
				, PARSE_ONCE());
		consumenode.addChild(new Pair<>(WHITESPACE, PARSE_MIN_ONCE()));
		consumenode.addChild(occurrencevalue, PARSE_ONCE());
		consumenode.addChild(new Pair<>(WHITESPACE, PARSE_ANY()));
		consumenode.addChild(semicolonskip, PARSE_ONCE());

		ContainerRule singlecallingparameter = factory.createValueRule("call_param")//
				.addChild(
						factory.createValueRule("type",
								factory.createMatchesRule(factory.pattern("regex|occurrence|rule")), PARSE_ONCE()),
						PARSE_ONCE())//
				.addChild(new Pair<>(WHITESPACE, PARSE_MIN_ONCE()))//
				.addChild(factory.createValueRule("name", declarednamematch, PARSE_ONCE()), PARSE_ONCE());
		ContainerRule calingparams = factory.createInOrderRule()//
				.addChild(factory.createSkipRule(factory.pattern("\\(")), PARSE_ONCE())//
				.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
				.addChild(factory.createInOrderRule()//
						.addChild(singlecallingparameter, PARSE_ONCE())//
						.addChild(factory.createInOrderRule()//
								.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
								.addChild(factory.createSkipRule(factory.pattern(",")), PARSE_ONCE())//
								.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
								.addChild(singlecallingparameter, PARSE_ONCE())//
								, PARSE_ANY())//
						, PARSE_OPTIONAL())//
				.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
				.addChild(factory.createSkipRule(factory.pattern("\\)")), PARSE_ONCE());

		ContainerRule containeroccurspec = factory.createInOrderRule()//
				.addChild(new Pair<>(WHITESPACE, PARSE_MIN_ONCE()))//
				.addChild(occurrencevalue, PARSE_ONCE());
		ContainerRule containerparamdefspec = factory.createInOrderRule()//
				.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
				.addChild(calingparams, PARSE_OPTIONAL());

		containernode.addChild(factory.createFirstOrderRule()//
				.addChild(factory.createInOrderRule()//
						.addChild(factory.createInOrderRule()//
								.addChild(
										factory.createValueRule("nonempty",
												factory.createMatchesRule(factory.pattern("non-empty")), PARSE_ONCE()),
										PARSE_ONCE())//
								.addChild(WHITESPACE, PARSE_MIN_ONCE())//
								, PARSE_OPTIONAL())//
						.addChild(factory.createValueRule("type", factory.createMatchesRule(factory.pattern("value")),
								PARSE_ONCE()), PARSE_ONCE())//
						.addChild(WHITESPACE, PARSE_MIN_ONCE())//
						.addChild(factory.createValueRule("name", declarednamematch, PARSE_ONCE()), PARSE_ONCE())//
						, PARSE_ONCE())//
				.addChild(factory.createInOrderRule()//
						.addChild(factory.createValueRule("type",
								factory.createMatchesRule(factory.pattern("inorder|anyorder|firstorder")),
								PARSE_ONCE()), PARSE_ONCE())//
						.addChild(factory.createInOrderRule()//
								.addChild(WHITESPACE, PARSE_MIN_ONCE())//
								.addChild(factory.createValueRule("name", declarednamematch, PARSE_ONCE()),
										PARSE_ONCE())//
								, PARSE_OPTIONAL())//
						, PARSE_ONCE())//
				, PARSE_ONCE());
		containernode.addChild(factory.createFirstOrderRule()//
				.addChild(containeroccurspec, PARSE_ONCE())//
				.addChild(containerparamdefspec, PARSE_ONCE())//
				, PARSE_OPTIONAL());
		containernode.addChild(new Pair<>(WHITESPACE, PARSE_ANY()));
		containernode.addChild(ordernodebody, PARSE_ONCE());

		final ContainerRule singleinvokeparam = factory.createFirstOrderRule()//
				.addChild(factory.createValueRule("param_ref", declarednamematch, PARSE_ONCE()), PARSE_ONCE())//
				.addChild(factory.createValueRule("param_occurrence", occurrencematch, PARSE_ONCE()), PARSE_ONCE())//
				.addChild(factory.createValueRule("param_regex", regexmatch, PARSE_ONCE()), PARSE_ONCE());//

		invokenode.addChild(factory.createInOrderRule()//
				.addChild(factory.createSkipRule(factory.pattern("include")), PARSE_ONCE())//
				.addChild(new Pair<>(WHITESPACE, PARSE_MIN_ONCE()))//
				.addChild(factory.createValueRule("name", declarednamematch, PARSE_ONCE()), PARSE_ONCE())//
				.addChild(factory.createInOrderRule()//
						.addChild(WHITESPACE, PARSE_MIN_ONCE())//
						.addChild(factory.createSkipRule(factory.pattern("as")), PARSE_ONCE())//
						.addChild(WHITESPACE, PARSE_MIN_ONCE())//
						.addChild(factory.createValueRule("alias_name", declarednamematch, PARSE_ONCE()), PARSE_ONCE())//
						, PARSE_OPTIONAL())//
				, PARSE_ONCE());
		invokenode.addChild(factory.createInOrderRule()//
				.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
				.addChild(factory.createSkipRule(factory.pattern("\\(")), PARSE_ONCE())//
				.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
				.addChild(factory.createValueRule("params")//
						.addChild(singleinvokeparam, PARSE_ONCE())//
						.addChild(factory.createInOrderRule()//
								.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
								.addChild(factory.createSkipRule(factory.pattern(",")), PARSE_ONCE())//
								.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
								.addChild(singleinvokeparam, PARSE_ONCE())//
								, PARSE_ANY())//
						, PARSE_OPTIONAL())//
				.addChild(new Pair<>(WHITESPACE, PARSE_ANY()))//
				.addChild(factory.createSkipRule(factory.pattern("\\)")), PARSE_ONCE())//
				, PARSE_OPTIONAL());
		invokenode.addChild(new Pair<>(WHITESPACE, PARSE_MIN_ONCE()));
		invokenode.addChild(occurrencevalue, PARSE_ONCE());
		invokenode.addChild(new Pair<>(WHITESPACE, PARSE_ANY()));
		invokenode.addChild(semicolonskip, PARSE_ONCE());

		generaldeclarations.addChild(new Pair<>(WHITESPACE, PARSE_ANY()));
		generaldeclarations.addChild(new Pair<>(invokenode, PARSE_ANY()));
		generaldeclarations.addChild(new Pair<>(containernode, PARSE_ANY()));
		generaldeclarations.addChild(new Pair<>(consumenode, PARSE_ANY()));
		generaldeclarations.addChild(new Pair<>(declarenode, PARSE_ANY()));
		generaldeclarations.addChild(new Pair<>(forwarddeclarenode, PARSE_ANY()));

		languagenode.addChild(factory.createSkipRule(factory.pattern("language")), PARSE_ONCE());
		languagenode.addChild(new Pair<>(WHITESPACE, PARSE_MIN_ONCE()));
		languagenode.addChild(factory.createValueRule("name", declarednamematch, PARSE_ONCE()), PARSE_ONCE());
		languagenode.addChild(new Pair<>(WHITESPACE, PARSE_ANY()));
		languagenode.addChild(ordernodebody, PARSE_ONCE());

		ContainerRule langcontentnode = factory.createAnyOrderRule()//
				.addChild(WHITESPACE, PARSE_ANY())//
				.addChild(languagenode, PARSE_ANY())//
		;

		DESCRIBER_LANGUAGE = new Language("descriptor_language", langcontentnode);
	}

	private static Pattern compileLanguagePattern(String pattern) {
		//add \A to match exactly the beginning of the input
		//https://docs.oracle.com/javase/tutorial/essential/regex/bounds.html
		//make the group we introduce non capturing, so references will be correct
		try {
			return Pattern.compile("\\A(?:" + pattern + ")");
		} catch (Exception e) {
			throw new RuntimeException("Failed to compile pattern: " + pattern, e);
		}
	}

	private static Pattern parseRegexpInput(RuleFactory factory, String input) throws ParseFailedException {
		try {
			String pattern = input.substring(1, input.length() - 1);
			return factory.pattern(pattern);
		} catch (PatternSyntaxException e) {
			throw new ParseFailedException("Failed to compile regular expression: \"" + input + "\"");
		}
	}

	private static InvokeParam<Occurrence> parseOccurrence(RuleFactory factory, Statement stm)
			throws ParseFailedException {
		Statement occurstm = stm.firstScope("occurrence");
		if (occurstm != null) {
			return new OccurrenceParam(factory.occurrence(occurstm.getValue()));
		}

		// check for variable reference
		Statement occurrefstm = stm.firstScope("occurrence_ref");
		if (occurrefstm != null) {
			return new VarReferenceParam<>(occurrefstm.getValue());
		}
		return null;
	}

	private static InvokeParam<Pattern> parseRegex(RuleFactory factory, Statement stm) throws ParseFailedException {
		Statement regexstm = stm.firstScope("regex");
		if (regexstm != null) {
			return new RegexParam(parseRegexpInput(factory, regexstm.getValue()));
		}

		// check for variable reference
		Statement regexrefstm = stm.firstScope("regex_ref");
		if (regexrefstm != null) {
			return new VarReferenceParam<>(regexrefstm.getValue());
		}
		return null;
	}

	private static Class<?> stringToTypename(String s) throws ParseFailedException {
		switch (s) {
			case "rule":
				return Rule.class;
			case "firstorder":
				return FirstOrderRule.class;
			case "anyorder":
				return AnyOrderRule.class;
			case "inorder":
				return InOrderRule.class;
			case "value":
				return ValueRule.class;
			case "skip":
				return SkipRule.class;
			case "matches":
				return MatchesRule.class;
			case "regex":
				return Pattern.class;
			case "occurrence":
				return Occurrence.class;
			default:
				throw new ParseFailedException("Invalid string typename: " + s);
		}
	}

	private static Object instantiateDeclaredObject(RuleFactory factory, String type, String value)
			throws ParseFailedException {
		switch (type) {
			case "occurrence":
				return factory.occurrence(value);
			case "regex":
				return parseRegexpInput(factory, value);
			default:
				throw new ParseFailedException("Invalid declared object type: " + value);
		}
	}

	private static ContainerRule instantiateContainerRule(RuleFactory factory, String type, String identifier)
			throws ParseFailedException {
		switch (type) {
			case "inorder":
				return factory.createInOrderRule(identifier);
			case "anyorder":
				return factory.createAnyOrderRule(identifier);
			case "firstorder":
				return factory.createFirstOrderRule(identifier);
			case "value":
				return factory.createValueRule(identifier);
			default:
				throw new ParseFailedException("Invalid container rule type: " + type);
		}
	}

	private static ConsumeRule instantiateConsumeRule(RuleFactory factory, String type, InvokeParam<Pattern> param,
			String alias) throws ParseFailedException {
		switch (type) {
			case "skip":
				return factory.createSkipRule(alias, param);
			case "matches":
				return factory.createMatchesRule(alias, param);
			default:
				throw new ParseFailedException("Invalid consume rule type: " + type);
		}
	}

	private static void parseDeclaredParams(Statement stm, Rule rule) throws ParseFailedException {
		List<Statement> scopedparams = stm.scopeTo("call_param");
		if (scopedparams.size() == 0)
			return;

		List<Pair<String, Class<?>>> result = rule.getDeclaredParams();
		for (Statement param : scopedparams) {
			Statement type = param.firstScope("type");
			Statement name = param.firstScope("name");
			result.add(new Pair<>(name.getValue(), stringToTypename(type.getValue())));
		}
	}

	private static List<InvokeParam<?>> parseInvokeParams(RuleFactory factory, Statement stm)
			throws ParseFailedException {
		List<InvokeParam<?>> target = new ArrayList<>();
		for (Pair<String, Statement> param : stm.getScopes()) {
			final String paramvalue = param.value.getValue();

			switch (param.key) {
				case "param_ref": {
					target.add(new VarReferenceParam<Object>(paramvalue));
					break;
				}
				case "param_occurrence": {
					target.add(new OccurrenceParam(factory.occurrence(paramvalue)));
					break;
				}
				case "param_regex": {
					target.add(new RegexParam(parseRegexpInput(factory, paramvalue)));
					break;
				}
				default: {
					throw new ParseFailedException("Invalid invoke param type: " + param.key);
				}
			}
		}
		return target;
	}

	private static void throwOnRedeclare(Stack<Pair<String, Object>> parseStack, String searchIdentifier)
			throws ParseFailedException {
		for (Pair<String, Object> pair : parseStack) {
			if (pair.key.equals(searchIdentifier)) {
				throw new ParseFailedException(
						"Entity redeclared: " + pair.value + " with identifier: " + searchIdentifier);
			}
		}
	}

	private static Object getObjectWithIdentifier(ArrayDeque<Pair<String, Object>> parseStack,
			String searchIdentifier) {
		for (Pair<String, Object> pair : parseStack) {
			if (pair.key.equals(searchIdentifier)) {
				return pair.value;
			}
		}
		return null;
	}

	private static void parseRules(Statement resultstm, Map<String, Language> langmap) throws ParseFailedException {
		RuleFactory factory = new RuleFactory();
		Set<Rule> undefinedrules = new HashSet<>();
		parseRulesImpl(langmap, null, resultstm, ruleStackWithDefaultRules(), undefinedrules, factory);
		if (!undefinedrules.isEmpty()) {
			throw new ParseFailedException("Some rules were not defined.");
		}
	}

	private static void parseRulesImpl(Map<String, Language> langmap, ContainerRule container, Statement stm,
			ArrayDeque<Pair<String, Object>> parseStack, Set<Rule> undefinedrules, RuleFactory factory)
			throws ParseFailedException {
		int parseStackAdded = 0;
		try {
			for (Pair<String, Statement> scopepair : stm.getScopes()) {
				Statement scoped = scopepair.value;
				switch (scopepair.key) {
					case "language_node": {
						final String name = scoped.firstScope("name").getValue();

						InOrderRule langrule = factory.createInOrderRule(name);
						parseRulesImpl(langmap, langrule, scoped.firstScope("body"), parseStack, undefinedrules,
								factory);
						String langid = name;
						Language lang = new Language(name, langrule);
						Language prev = langmap.put(langid, lang);
						if (prev != null) {
							throw new IllegalArgumentException("Duplicate language definitions: " + langid);
						}

						lang.rule = langrule;
						break;
					}
					case "invoke_node": {
						final InvokeParam<Occurrence> occurrence = parseOccurrence(factory, scoped);
						final String invokename = scoped.firstScope("name").getValue();
						final Statement aliasstm = scoped.firstScope("alias_name");
						final String alias = aliasstm == null ? null : aliasstm.getValue();

						Statement paramsstm = scoped.firstScope("params");
						List<InvokeParam<?>> invokeparams;
						if (paramsstm != null) {
							invokeparams = parseInvokeParams(factory, paramsstm);
						} else {
							invokeparams = Collections.emptyList();
						}
						final Rule rule = factory.createInvokeRule(new VarReferenceParam<>(invokename), alias,
								invokeparams);

						container.addChild(rule, new ParseTimeData(occurrence, new DeclaringContext(parseStack)));
						break;
					}
					case "container_node": {
						final InvokeParam<Occurrence> occurrence = parseOccurrence(factory, scoped);
						final String type = scoped.firstScope("type").getValue();
						final Statement namestm = scoped.firstScope("name");
						final String name = namestm == null ? null : namestm.getValue();
						final ContainerRule rule;

						if (name != null) {
							RuleDeclaration found = (RuleDeclaration) getObjectWithIdentifier(parseStack, name);
							if (found != null) {
								// found in stack
								Rule foundrule = found.getRule();
								if (found.getDeclarationContext() != null) {
									throw new ParseFailedException("Container node was already defined previously: "
											+ name + " redeclare at: " + scoped.toDocumentPositionString());
								}
								if (foundrule.getClass() != stringToTypename(type)) {
									throw new ParseFailedException(
											"Container node was declared previously with different type: "
													+ found.getClass().getSimpleName() + " - " + stringToTypename(type)
													+ " - " + name);
								}
								found.setDeclarationContext(new DeclaringContext(parseStack));

								if (!undefinedrules.remove(foundrule)) {
									throw new ParseFailedException("Container node was already defined previously: "
											+ name + " redeclare at: " + scoped.toDocumentPositionString());
								}
								rule = (ContainerRule) foundrule;
							} else {
								// was not in stack
								rule = instantiateContainerRule(factory, type, name);
								if ("value".equals(type)) {
									ValueRule vr = (ValueRule) rule;
									if (scoped.firstScope("nonempty") != null) {
										vr.setNonEmpty(true);
									}
								}
								parseStack.push(new Pair<>(rule.getIdentifierName(),
										new RuleDeclaration(rule, new DeclaringContext(parseStack))));
								++parseStackAdded;
							}
						} else {
							rule = instantiateContainerRule(factory, type, name);
						}

						parseDeclaredParams(scoped, rule);
						for (Pair<String, Class<?>> param : rule.getDeclaredParams()) {
							parseStack.add(new Pair<>(param.key,
									new VarReferenceParam<>(rule.createParameterName(param.key))));
							++parseStackAdded;
						}

						if (occurrence != null) {
							container.addChild(rule, new ParseTimeData(occurrence, new DeclaringContext(parseStack)));
						}
						if (name == null && occurrence == null) {
							throw new ParseFailedException("Container rule must have name or occurrence");
						}

						parseRulesImpl(langmap, rule, scoped.firstScope("body"), parseStack, undefinedrules, factory);
						break;
					}
					case "consume_node": {
						final String type = scoped.firstScope("type").getValue();
						final InvokeParam<Occurrence> occurrence = parseOccurrence(factory, scoped);
						final InvokeParam<Pattern> param = parseRegex(factory, scoped);
						final String alias = scoped.firstValue("alias_name");
						final Rule rule = instantiateConsumeRule(factory, type, param, alias);

						container.addChild(rule, new ParseTimeData(occurrence, new DeclaringContext(parseStack)));
						break;
					}
					case "declare_node": {
						final String type = scoped.firstScope("type").getValue();
						final String name = scoped.firstScope("name").getValue();
						final String value = scoped.firstScope("expression").getValue();
						final Object parsed = instantiateDeclaredObject(factory, type, value);

						parseStack.push(new Pair<>(name, parsed));
						++parseStackAdded;
						break;
					}
					case "forwarddeclare_container_node": {
						final String type = scoped.firstScope("type").getValue();
						final String name = scoped.firstScope("name").getValue();

						Object found = getObjectWithIdentifier(parseStack, name);
						if (found != null) {
							// found object, make sure the classes match
							if (found.getClass() != stringToTypename(type)) {
								throw new ParseFailedException(
										"Container node was declared previously with different type: "
												+ found.getClass().getSimpleName());
							}
							// do nothing
						} else {
							// instantiate
							ContainerRule rule = instantiateContainerRule(factory, type, name);
							undefinedrules.add(rule);
							// add to stack
							parseStack.push(new Pair<>(rule.getIdentifierName(), new RuleDeclaration(rule)));
							++parseStackAdded;
						}
						break;
					}
					default: {
						break;
					}
				}
			}
		} finally {
			while (parseStackAdded-- > 0) {
				parseStack.pop();
			}
		}
	}

	private static ArrayDeque<Pair<String, Object>> addDefaultRulesToStack(ArrayDeque<Pair<String, Object>> stack) {
		return stack;
	}

	private static ArrayDeque<Pair<String, Object>> ruleStackWithDefaultRules() {
		return addDefaultRulesToStack(new ArrayDeque<>());
	}

	private static String readStreamStringFully(InputStream is) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024 * 8];

			for (int len; (len = is.read(buffer)) > 0;) {
				os.write(buffer, 0, len);
			}

			return os.toString("UTF-8");
		}
	}

	public static Map<String, Language> fromFile(File f) throws IOException, ParseFailedException {
		return fromPath(f.toPath());
	}

	public static Map<String, Language> fromPath(Path p) throws IOException, ParseFailedException {
		return fromString(new String(Files.readAllBytes(p), StandardCharsets.UTF_8));
	}

	public static Map<String, Language> fromInputStream(InputStream is) throws IOException, ParseFailedException {
		return fromString(readStreamStringFully(is));
	}

	public static Map<String, Language> fromString(String data) throws ParseFailedException {
		ParsingResult result = DESCRIBER_LANGUAGE.parseData(data);
		Statement resultstm = result.getStatement();

		if (resultstm.getEndOffset() != data.length()) {
			throw new ParseFailedException("Failed to parse whole file");
		}

		Map<String, Language> langmap = new TreeMap<>();

		parseRules(resultstm, langmap);

		return langmap;
	}

	private transient String name;
	private ContainerRule rule;

	public Language(String name, ContainerRule rule) {
		this.name = name;
		this.rule = rule;
	}

	public ParsingResult parseFile(File f) throws ParseFailedException, IOException {
		String data = new String(Files.readAllBytes(f.toPath()));
		return parseData(data);
	}

	public ParsingResult parseInputStream(InputStream is) throws ParseFailedException, IOException {
		return parseData(readStreamStringFully(is));
	}

	public ParsingResult parseInputStream(InputStream is, ParseProgressMonitor progressmonitor)
			throws ParseFailedException, IOException {
		return parseData(readStreamStringFully(is), System.err, progressmonitor);
	}

	public ParsingResult parseData(char[] data, PrintStream infostream, ParseProgressMonitor progressmonitor)
			throws ParseFailedException {
		//TODO remove this infostream and move data to thrown exception
		ParsingResult result;
		ParseHelper helper;
		try {
			DocumentData docdata = new DocumentData(data);
			helper = new ParseHelper();
			if (progressmonitor != null) {
				helper.setProgressMonitor(progressmonitor);
			}
			result = rule.parseStatement(helper, docdata, ParseContext.EMPTY, PARSE_ONCE());
		} catch (FatalParseException e) {
			throw e;
		}
		Statement resultstm = result.getStatement();
		if (resultstm == null || resultstm.getEndOffset() != data.length) {
//					Set<ParseFail> fails = helper.getFails();
//					String posstring = resultstm.getEndOffset() + "";
			//
//					if (infostream != null) {
//						infostream.println("Parsed: ");
//						resultstm.prettyprint(infostream);
//						infostream.println("Parse error info:");
//					}
			//TODO DIAGNOSTICS
//					if (!fails.isEmpty()) {
////						final int line = helper.getPosition().getLine();
////						final int positionInLine = helper.getPosition().getPositionInLine();
//						final int posfromstart = helper.getDocumentOffset();
			//
//						posstring = posfromstart + "";//(line + 1) + ":" + (positionInLine + 1);
//						if (infostream != null) {
//							int nlineafter = data.indexOf('\n', posfromstart) - 1;
//							if (nlineafter < 0) {
//								nlineafter = data.length();
//							}
//							infostream.println(
//									"Expected at " + posstring + ": \"" + data.substring(posfromstart, nlineafter) + "\"");
//							for (ParseFail fail : fails) {
//								infostream.print("\t" + "Pattern: \"" + fail.getPattern() + "\"");
//								if (fail.getIdentifier() != null) {
//									infostream.print(" Identifier: \"" + fail.getIdentifier() + "\"");
//								}
//								infostream.println();
//								for (ListIterator<Rule> it = fail.getStack().listIterator(fail.getStack().size()); it
//										.hasPrevious();) {
//									Rule st = it.previous();
//									if (st.getIdentifierName() != null) {
//										DocumentRegion stpos = st.getRuleDocumentPosition();
//										if (stpos != null) {
//											infostream.println("\t\t" + st.getIdentifierName() + " at: " + stpos// + (stpos.getLine() + 1) + ":" + (stpos.getPositionInLine() + 1)
//											);
//										}
//									}
//								}
//							}
//						}
//					} else {
//						posstring = resultstm.getPosition() + "";// (resultstm.getEndPos().getLine() + 1) + ":" + (resultstm.getEndPos().getPositionInLine() + 1);
//						if (infostream != null) {
//							infostream.println("No matching rule found for parsing remaining data");
//						}
//					}
			//TODO reify error message
			throw new ParseFailedException("Failed to parse input.");
//					throw new ParseFailedException("Failed to parse data, no rule to parse from line: " + posstring
//							+ " Check output for more info. (Data length: " + data.length() + ", Parsed:"
//							+ resultstm.getEndOffset() + ")");
		}
		return result;
	}

	public ParsingResult parseData(String data, PrintStream infostream, ParseProgressMonitor progressmonitor)
			throws ParseFailedException {
		return parseData(data.toCharArray(), infostream, progressmonitor);
	}

	public ParsingResult parseData(String data, PrintStream infostream) throws ParseFailedException {
		return parseData(data, infostream, ParseProgressMonitor.NULLMONITOR);
	}

	public ParsingResult parseData(String data) throws ParseFailedException {
		return parseData(data, System.err);
	}

	public ParsingResult parseData(char[] data) throws ParseFailedException {
		return parseData(data, System.err, ParseProgressMonitor.NULLMONITOR);
	}

	@Override
	public int hashCode() {
		return rule.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + name + "]";
	}
}
