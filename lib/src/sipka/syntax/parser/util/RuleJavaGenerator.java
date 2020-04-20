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
package sipka.syntax.parser.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import sipka.syntax.parser.model.occurrence.Occurrence;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.DeclaringContext;
import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.parse.params.InvokeParamVisitor;
import sipka.syntax.parser.model.parse.params.OccurrenceParam;
import sipka.syntax.parser.model.parse.params.RegexParam;
import sipka.syntax.parser.model.parse.params.RuleInvocationVarReferenceParam;
import sipka.syntax.parser.model.parse.params.VarReferenceParam;
import sipka.syntax.parser.model.rule.Language;
import sipka.syntax.parser.model.rule.Rule;
import sipka.syntax.parser.model.rule.RuleDeclaration;
import sipka.syntax.parser.model.rule.RuleFactory;
import sipka.syntax.parser.model.rule.RuleVisitor;
import sipka.syntax.parser.model.rule.consume.MatchesRule;
import sipka.syntax.parser.model.rule.consume.SkipRule;
import sipka.syntax.parser.model.rule.container.ContainerRule;
import sipka.syntax.parser.model.rule.container.order.AnyOrderRule;
import sipka.syntax.parser.model.rule.container.order.FirstOrderRule;
import sipka.syntax.parser.model.rule.container.order.InOrderRule;
import sipka.syntax.parser.model.rule.container.value.ValueRule;
import sipka.syntax.parser.model.rule.invoke.InvokeRule;

public class RuleJavaGenerator {

	public static String generateLanguageJavaClass(String classname, Map<String, Language> languages) {
		StringBuilder sb = new StringBuilder();
		String simplename = appendPackageDeclarationGetSimpleName(classname, sb);
		sb.append("public class ");
		sb.append(simplename);
		sb.append(" {\n");

		for (Entry<String, Language> entry : languages.entrySet()) {
			String body = generateLanguageJavaSourceMethodBody(entry.getValue());
			String languagename = entry.getKey();

			appendLanguageRetrievalMethod(sb, body, languagename);
		}

		sb.append("}\n");
		return sb.toString();
	}

	private static void appendLanguageRetrievalMethod(StringBuilder sb, String body, String languagename) {
		sb.append("@SuppressWarnings({ \"rawtypes\", \"unchecked\" })\n");
		sb.append("public static ");
		sb.append(Language.class.getCanonicalName());
		sb.append(" get");
		sb.append(languagename);
		sb.append("() {\n");
		sb.append(body);
		sb.append("return language;\n");
		sb.append("}\n");
	}

	public static String generateLanguageJavaClass(Language lang, String classname) {
		String langbody = generateLanguageJavaSourceMethodBody(lang);

		StringBuilder sb = new StringBuilder();
		String simplename = appendPackageDeclarationGetSimpleName(classname, sb);
		sb.append("public class ");
		sb.append(simplename);
		sb.append(" {\n");
		appendLanguageRetrievalMethod(sb, langbody, "Language");

		sb.append("}\n");
		return sb.toString();
	}

	private static String appendPackageDeclarationGetSimpleName(String classname, StringBuilder sb) {
		int dotidx = classname.lastIndexOf('.');
		String simplename = classname.substring(dotidx + 1);
		String pkg;
		if (dotidx < 0) {
			pkg = null;
		} else {
			pkg = classname.substring(0, dotidx);
		}
		if (pkg != null) {
			sb.append("package ");
			sb.append(pkg);
			sb.append(";\n\n");
		}
		return simplename;
	}

	public static String generateLanguageJavaSourceMethodBody(Language lang) {
		Objects.requireNonNull(lang, "language");
		ContainerRule baserule = lang.getRule();
		StringBuilder sb = new StringBuilder();
		sb.append(Language.class.getCanonicalName());
		sb.append(" language;\n");
		sb.append("{\n");
		sb.append(RuleFactory.class.getCanonicalName());
		sb.append(" factory = new ");
		sb.append(RuleFactory.class.getCanonicalName());
		sb.append("();\n");

		int predecllen = sb.length();

		VariablesCache vc = new VariablesCache();

		new RuleDetailsFillerVisitor(vc, sb).printChildren(baserule);
		vc.fillRuleDeclarations(sb);

		String baserulevarname = vc.getRule(baserule);

		sb.append("language = new ");
		sb.append(Language.class.getCanonicalName());
		sb.append("(");
		appendStringLiteral(sb, lang.getName());
		sb.append(", ");
		sb.append(baserulevarname);
		sb.append(");\n");
		sb.append("}\n");
		sb.insert(predecllen, vc.getVariablesString());
		return sb.toString();
	}

	private static String getParamVar(VariablesCache vc, InvokeParam<?> param) {
		String[] result = { null };
		param.accept(new InvokeParamVisitor() {
			@Override
			public void visit(VarReferenceParam<?> param) {
				result[0] = vc.getVarReferenceParam(param.getVariableName());
			}

			@Override
			public void visit(OccurrenceParam param) {
				result[0] = vc.getOccurrence(param.getOccurrence());
			}

			@Override
			public void visit(RegexParam param) {
				result[0] = vc.getPattern(param.getPattern());
			}

			@Override
			public void visit(RuleInvocationVarReferenceParam<?> param) {
				result[0] = vc.getRuleInvocationVarReferenceParam(param);
			}
		});
		return result[0];
	}

	private static void appendParam(VariablesCache vc, StringBuilder sb, InvokeParam<?> param) {
		if (param == null) {
			sb.append("null");
		} else {
			sb.append(getParamVar(vc, param));
		}
	}

	private static void appendStringLiteral(StringBuilder sb, String idname) {
		if (idname == null) {
			sb.append("null");
		} else {
			appendConstantExpression(idname, sb);
		}
	}

	public static String getConstantExpression(String s) {
		int slen = s.length();
		StringBuilder buf = new StringBuilder(slen * 2);
		appendConstantExpression(s, buf);
		return buf.toString();
	}

	private static void appendConstantExpression(String s, StringBuilder buf) {
		int slen = s.length();
		buf.append('\"');
		for (int i = 0; i < slen; i++) {
			buf.append(toEscapedCharacter(s.charAt(i)));
		}
		buf.append('\"');
	}

	private static String toEscapedCharacter(char ch) {
		if (ch >= 32 && ch <= 126) {
			switch (ch) {
				case '\'': {
					return "\\'";
				}
				case '\"': {
					return "\\\"";
				}
				case '\\': {
					return "\\\\";
				}
				default: {
					return String.valueOf(ch);
				}
			}
		}
		switch (ch) {
			case '\b': {
				return "\\b";
			}
			case '\f': {
				return "\\f";
			}
			case '\n': {
				return "\\n";
			}
			case '\r': {
				return "\\r";
			}
			case '\t': {
				return "\\t";
			}
			default: {
				return String.format("\\u%04x", (int) ch);
			}
		}
	}

	private static class VariablesCache {
		private StringBuilder sb = new StringBuilder();

		private Map<String, Integer> occurrences = new TreeMap<>();
		private Map<String, Integer> patterns = new TreeMap<>();
		private Map<DeclaringContext, Integer> declarationContexts = new HashMap<>();
		private Map<String, Integer> varrefs = new TreeMap<>();
		private Map<RuleInvocationVarReferenceParam<?>, Integer> ruleVarRefs = new HashMap<>();

		private Map<RuleDeclaration, Integer> ruleDecls = new HashMap<>();

		private Map<Pair<String, String>, Integer> pairVars = new HashMap<>();
		private Map<Pair<String, String>, Integer> parseTimeDatasVars = new HashMap<>();

		private Map<Rule, Integer> ruleVars = new HashMap<>();

		public VariablesCache() {
		}

		public String getRule(Rule r) {
			int size = ruleVars.size();
			Integer present = ruleVars.putIfAbsent(r, size);
			if (present != null) {
				return "rule" + present;
			}
			StringBuilder sb = new StringBuilder();
			RuleHeaderVisitor headerVisitor = new RuleHeaderVisitor(this, sb, size);
			headerVisitor.printHeader(r);
			this.sb.append(sb);
			return "rule" + size;
		}

		public String getParseTimeData(String occurrencevar, String contextvar) {
			Pair<String, String> p = new Pair<>(occurrencevar, contextvar);
			int size = parseTimeDatasVars.size();
			Integer present = parseTimeDatasVars.putIfAbsent(p, size);
			if (present != null) {
				return "ptd" + present;
			}
			sb.append(ParseTimeData.class.getCanonicalName());
			sb.append(" ptd");
			sb.append(size);
			sb.append(" = new ");
			sb.append(ParseTimeData.class.getCanonicalName());
			sb.append("(");
			sb.append(occurrencevar);
			if (contextvar != null) {
				sb.append(", ");
				sb.append(contextvar);
			}
			sb.append(");\n");
			return "ptd" + size;
		}

		public String getStringAndUnescapedPair(String keystr, String var) {
			Pair<String, String> p = new Pair<>(keystr, var);
			int size = pairVars.size();
			Integer present = pairVars.putIfAbsent(p, size);
			if (present != null) {
				return "pair" + present;
			}
			sb.append(Pair.class.getCanonicalName());
			sb.append(" pair");
			sb.append(size);
			sb.append(" = new ");
			sb.append(Pair.class.getCanonicalName());
			sb.append("(");
			appendStringLiteral(sb, keystr);
			sb.append(", ");
			sb.append(var);
			sb.append(");\n");
			return "pair" + size;
		}

		public void fillRuleDeclarations(StringBuilder sb) {
			outloop:
			while (true) {
				for (RuleDeclaration rd : ruleDecls.keySet()) {
					DeclaringContext dc = rd.getDeclarationContext();
					if (dc == null || DeclaringContext.EMPTY.equals(dc)) {
						continue;
					}
					if (!declarationContexts.containsKey(dc)) {
						getDeclarationContext(dc);
						continue outloop;
					}
				}
				for (Entry<RuleDeclaration, Integer> entry : ruleDecls.entrySet()) {
					RuleDeclaration rd = entry.getKey();
					DeclaringContext dc = rd.getDeclarationContext();
					if (dc == null) {
						continue;
					}
					sb.append("rdecl");
					sb.append(entry.getValue());
					sb.append(".setDeclarationContext(");
					sb.append(getDeclarationContext(dc));
					sb.append(");\n");
				}
				break;
			}
		}

		public String getRuleDeclaration(RuleDeclaration rd) {
			int size = ruleDecls.size();
			Integer present = ruleDecls.putIfAbsent(rd, size);
			if (present != null) {
				return "rdecl" + present;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(RuleDeclaration.class.getCanonicalName());
			sb.append(" rdecl");
			sb.append(size);
			sb.append(" = new ");
			sb.append(RuleDeclaration.class.getCanonicalName());
			sb.append("(");

			sb.append(this.getRule(rd.getRule()));

			sb.append(");\n");
			this.sb.append(sb);
			return "rdecl" + size;
		}

		public String getRuleInvocationVarReferenceParam(RuleInvocationVarReferenceParam<?> param) {

			int size = ruleVarRefs.size();
			Integer present = ruleVarRefs.putIfAbsent(param, size);
			if (present != null) {
				return "rivref" + present;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(RuleInvocationVarReferenceParam.class.getCanonicalName());
			sb.append(" rivref");
			sb.append(size);
			sb.append(" = new ");
			sb.append(RuleInvocationVarReferenceParam.class.getCanonicalName());
			sb.append("(");
			sb.append(getRule(param.getRule()));
			sb.append(", ");
			appendStringLiteral(sb, param.getVariableName());
			sb.append(");\n");
			this.sb.append(sb);
			return "rivref" + size;
		}

		public String getVarReferenceParam(String name) {
			int size = varrefs.size();
			Integer present = varrefs.putIfAbsent(name, size);
			if (present != null) {
				return "vref" + present;
			}
			sb.append(VarReferenceParam.class.getCanonicalName());
			sb.append(" vref");
			sb.append(size);
			sb.append(" = new ");
			sb.append(VarReferenceParam.class.getCanonicalName());
			sb.append("(");
			appendStringLiteral(sb, name);
			sb.append(");\n");
			return "vref" + size;
		}

		public String getDeclarationContext(DeclaringContext declcontext) {
			if (declcontext == null) {
				return null;
			}
			if (DeclaringContext.EMPTY.equals(declcontext)) {
				return DeclaringContext.class.getCanonicalName() + ".EMPTY";
			}
			int size = declarationContexts.size();
			Integer present = declarationContexts.putIfAbsent(declcontext, size);
			if (present != null) {
				return "declcontext" + present;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(DeclaringContext.class.getCanonicalName());
			sb.append(" declcontext");
			sb.append(size);
			sb.append(" = new ");
			sb.append(DeclaringContext.class.getCanonicalName());
			sb.append("(");
			Set<Entry<String, Object>> localsentries = declcontext.getLocalsMap().entrySet();
			appendAsListStart(sb, localsentries);
			for (Iterator<Entry<String, Object>> it = localsentries.iterator(); it.hasNext();) {
				Entry<String, Object> entry = it.next();
				String paramvar;
				Object val = entry.getValue();
				if (val instanceof Occurrence) {
					paramvar = this.getOccurrence((Occurrence) val);
				} else if (val instanceof RuleDeclaration) {
					paramvar = this.getRuleDeclaration((RuleDeclaration) val);
				} else if (val instanceof Pattern) {
					paramvar = this.getPattern(((Pattern) val));
				} else if (val instanceof InvokeParam<?>) {
					paramvar = getParamVar(this, (InvokeParam<?>) val);
				} else {
					throw new AssertionError("Unrecognized local object: " + entry.getKey() + " : "
							+ val.getClass().getName() + " - " + val);
				}
				sb.append(getStringAndUnescapedPair(entry.getKey(), paramvar));
				if (it.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append("));\n");
			this.sb.append(sb);
			return "declcontext" + size;
		}

		public String getOccurrence(Occurrence occur) {
			return getOccurrence(occur.toString());
		}

		public String getOccurrence(String occstr) {
			int size = occurrences.size();
			Integer present = occurrences.putIfAbsent(occstr, size);
			if (present != null) {
				return "occ" + present;
			}
			sb.append(OccurrenceParam.class.getCanonicalName());
			sb.append(" occ");
			sb.append(size);
			sb.append(" = new ");
			sb.append(OccurrenceParam.class.getCanonicalName());
			sb.append("(factory.occurrence(");
			appendStringLiteral(sb, occstr);
			sb.append("));\n");
			return "occ" + size;
		}

		public String getPattern(Pattern pattern) {
			return getPattern(pattern.pattern());
		}

		public String getPattern(String patternstr) {
			int size = patterns.size();
			Integer present = patterns.putIfAbsent(patternstr, size);
			if (present != null) {
				return "pattern" + present;
			}
			sb.append(RegexParam.class.getCanonicalName());
			sb.append(" pattern");
			sb.append(size);
			sb.append(" = new ");
			sb.append(RegexParam.class.getCanonicalName());
			sb.append("(java.util.regex.Pattern.compile(");
			appendStringLiteral(sb, patternstr);
			sb.append("));\n");
			return "pattern" + size;
		}

		public CharSequence getVariablesString() {
			return sb;
		}

	}

	private static final class RuleDetailsFillerVisitor implements RuleVisitor {
		private final VariablesCache vc;
		private final StringBuilder sb;
		private Set<Rule> printedRules = new HashSet<>();
		private Set<DeclaringContext> printedParseContexts = Collections.newSetFromMap(new IdentityHashMap<>());

		public RuleDetailsFillerVisitor(VariablesCache vc, StringBuilder sb) {
			this.vc = vc;
			this.sb = sb;
		}

		public void printChildren(Rule rule) {
			if (!printedRules.add(rule)) {
				return;
			}
			rule.accept(this);
			appendDeclaredParams(sb, vc, rule);
		}

		@Override
		public void visit(MatchesRule rule) {
		}

		@Override
		public void visit(SkipRule rule) {
		}

		@Override
		public void visit(AnyOrderRule rule) {
			visitContainer((ContainerRule) rule);
		}

		@Override
		public void visit(FirstOrderRule rule) {
			visitContainer((ContainerRule) rule);
		}

		@Override
		public void visit(InOrderRule rule) {
			visitContainer((ContainerRule) rule);
		}

		@Override
		public void visit(ValueRule rule) {
			visitContainer((ContainerRule) rule);
		}

		@Override
		public void visit(InvokeRule rule) {
		}

		public void visitContainer(ContainerRule rule) {
			for (Pair<Rule, ParseTimeData> cp : rule.getChildren()) {
				DeclaringContext declcontext = cp.value.getDeclaringContext();

				String occurrencevar = getParamVar(vc, cp.value.getOccurrenceParam());
				String declcontetvar = vc.getDeclarationContext(declcontext);

				sb.append(vc.getRule(rule));
				sb.append(".addChild(");
				sb.append(vc.getRule(cp.key));
				sb.append(", ");
				sb.append(vc.getParseTimeData(occurrencevar, declcontetvar));
				sb.append(");\n");
			}
			for (Pair<Rule, ParseTimeData> cp : rule.getChildren()) {
				printChildren(cp.key);
				printParseContext(cp.value.getDeclaringContext());
			}
		}

		private void printParseContext(DeclaringContext parsecontext) {
			if (!printedParseContexts.add(parsecontext)) {
				return;
			}
			for (Object l : parsecontext.getLocalsMap().values()) {
				if (l instanceof RuleDeclaration) {
					RuleDeclaration ruledecl = (RuleDeclaration) l;
					printChildren(ruledecl.getRule());
					printParseContext(ruledecl.getDeclarationContext());
				}
			}
		}
	}

	private static final class RuleHeaderVisitor implements RuleVisitor {
		private final VariablesCache vc;
		private final StringBuilder sb;
		private final int idx;

		public RuleHeaderVisitor(VariablesCache vc, StringBuilder sb, int idx) {
			this.vc = vc;
			this.sb = sb;
			this.idx = idx;
		}

		public void printHeader(Rule rule) {
			rule.accept(this);
		}

		@Override
		public void visit(InvokeRule rule) {
			String idname = rule.getIdentifierName();
			String alias = rule.getAlias();
			List<InvokeParam<?>> invokeparams = rule.getInvokeParams();

			sb.append(InvokeRule.class.getCanonicalName());
			sb.append(" rule");
			sb.append(idx);
			sb.append(" = factory.createInvokeRule(");
			if (idname != null) {
				appendStringLiteral(sb, idname);
				sb.append(", ");
			}
			appendParam(vc, sb, rule.getRuleParam());
			if (alias != null) {
				sb.append(", ");
				appendStringLiteral(sb, alias);
			}
			if (!invokeparams.isEmpty()) {
				sb.append(", ");
				appendAsListStart(sb, invokeparams);
				for (Iterator<InvokeParam<?>> it = invokeparams.iterator(); it.hasNext();) {
					InvokeParam<?> ip = it.next();
					appendParam(vc, sb, ip);
					if (it.hasNext()) {
						sb.append(", ");
					}
				}
				sb.append(")");
			}
			sb.append(");\n");
		}

		@Override
		public void visit(ValueRule rule) {
			sb.append(ValueRule.class.getCanonicalName());
			sb.append(" rule");
			sb.append(idx);
			sb.append(" = factory.createValueRule(");
			String idname = rule.getIdentifierName();
			appendStringLiteral(sb, idname);
			sb.append(");\n");
			if (rule.isNonEmpty()) {
				sb.append("rule");
				sb.append(idx);
				sb.append(".setNonEmpty(true);\n");
			}
		}

		@Override
		public void visit(InOrderRule rule) {
			sb.append(InOrderRule.class.getCanonicalName());
			sb.append(" rule");
			sb.append(idx);
			sb.append(" = factory.createInOrderRule(");
			String idname = rule.getIdentifierName();
			if (idname != null) {
				appendStringLiteral(sb, idname);
			}
			sb.append(");\n");
		}

		@Override
		public void visit(FirstOrderRule rule) {
			sb.append(FirstOrderRule.class.getCanonicalName());
			sb.append(" rule");
			sb.append(idx);
			sb.append(" = factory.createFirstOrderRule(");
			String idname = rule.getIdentifierName();
			if (idname != null) {
				appendStringLiteral(sb, idname);
			}
			sb.append(");\n");
		}

		@Override
		public void visit(AnyOrderRule rule) {
			sb.append(AnyOrderRule.class.getCanonicalName());
			sb.append(" rule");
			sb.append(idx);
			sb.append(" = factory.createAnyOrderRule(");
			String idname = rule.getIdentifierName();
			if (idname != null) {
				appendStringLiteral(sb, idname);
			}
			sb.append(");\n");
		}

		@Override
		public void visit(SkipRule rule) {
			sb.append(SkipRule.class.getCanonicalName());
			sb.append(" rule");
			sb.append(idx);
			sb.append(" = factory.createSkipRule(");
			String idname = rule.getIdentifierName();
			if (idname != null) {
				appendStringLiteral(sb, idname);
				sb.append(", ");
			}
			appendParam(vc, sb, rule.getParam());
			sb.append(");\n");
		}

		@Override
		public void visit(MatchesRule rule) {
			sb.append(MatchesRule.class.getCanonicalName());
			sb.append(" rule");
			sb.append(idx);
			sb.append(" = factory.createMatchesRule(");
			String idname = rule.getIdentifierName();
			if (idname != null) {
				appendStringLiteral(sb, idname);
				sb.append(", ");
			}
			appendParam(vc, sb, rule.getParam());
			sb.append(");\n");
		}

	}

	private static void appendDeclaredParams(StringBuilder sb, VariablesCache vc, Rule rule) {
		List<Pair<String, Class<?>>> dp = rule.getDeclaredParams();
		if (dp == null || dp.isEmpty()) {
			return;
		}
		sb.append("{\n");
		sb.append("java.util.List<sipka.syntax.parser.util.Pair<String, Class<?>>> dp = ");
		sb.append(vc.getRule(rule));
		sb.append(".getDeclaredParams();\n");
		for (Pair<String, Class<?>> p : dp) {
			sb.append("dp.add(");
			sb.append(vc.getStringAndUnescapedPair(p.key, p.value.getCanonicalName() + ".class"));
			sb.append(");\n");
		}
		sb.append("}\n");
	}

	private static void appendAsListStart(StringBuilder sb, Collection<?> invokeparams) {
		if (invokeparams.size() == 1) {
			sb.append("java.util.Collections.singletonList(");
		} else {
			sb.append("java.util.Arrays.asList(");
		}
	}
}
