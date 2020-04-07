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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.rule.Language.ParseProgressMonitor;

public class ParseHelper {
	public static class ParseFail {
		private final String pattern;
		private final String identifier;
		private final List<Rule> ruleStack;

		public ParseFail(String pattern, String identifier, Collection<Rule> ruleStack) {
			this.pattern = pattern;
			this.identifier = identifier;
			this.ruleStack = Arrays.asList(ruleStack.toArray(new Rule[ruleStack.size()]));
		}

		public String getIdentifier() {
			return identifier;
		}

		public String getPattern() {
			return pattern;
		}

		public List<Rule> getStack() {
			return ruleStack;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
			result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ParseFail other = (ParseFail) obj;
			if (identifier == null) {
				if (other.identifier != null)
					return false;
			} else if (!identifier.equals(other.identifier))
				return false;
			if (pattern == null) {
				if (other.pattern != null)
					return false;
			} else if (!pattern.equals(other.pattern))
				return false;
			return true;
		}
	}

	public static class RuleParseStateKey {
		private final Rule rule;
		private final int offset;

		public RuleParseStateKey(Rule rule, int offset) {
			this.rule = rule;
			this.offset = offset;
		}

		@Override
		public int hashCode() {
			return rule.hashCode() ^ offset;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RuleParseStateKey other = (RuleParseStateKey) obj;
			if (offset != other.offset)
				return false;
			if (!rule.equals(other.rule))
				return false;
			return true;
		}
	}

	public static class RuleParseStateValue {
		private ParsingResult result;
		private ParseContext context;

		public RuleParseStateValue(ParsingResult result, ParseContext context) {
			this.result = result;
			this.context = context;
		}

		public boolean isSuitableContext(ParseContext context) {
			return context.equals(this.context);
		}

		public ParsingResult getResult() {
			return result;
		}
	}

	private Set<ParseFail> fails = new HashSet<>();

	private ParseProgressMonitor monitor = ParseProgressMonitor.NULLMONITOR;

	private final Map<RuleParseStateKey, Collection<RuleParseStateValue>> parsedRulesCache = new HashMap<>();

	private final Map<Pattern, Matcher> matcherCache = new HashMap<>();

	public ParseHelper() {
	}

	public Matcher getMatcher(Pattern pattern, CharSequence input) {
		return matcherCache.compute(pattern, (p, m) -> {
			if (m == null) {
				return p.matcher(input);
			}
			m.reset(input);
			return m;
		});
	}

	public ParsingResult getExistingParseResult(Rule rule, int offset, ParseContext context) {
		RuleParseStateKey key = new RuleParseStateKey(rule, offset);
		Collection<RuleParseStateValue> states = parsedRulesCache.get(key);
		if (states == null) {
			return null;
		}
		for (RuleParseStateValue sval : states) {
			if (sval.isSuitableContext(context)) {
				return sval.getResult();
			}
		}
		return null;
	}

	public void ruleParsed(Rule rule, ParsingResult result, int offset, ParseContext context) {
		RuleParseStateKey key = new RuleParseStateKey(rule, offset);
		RuleParseStateValue stateval = new RuleParseStateValue(result, context);
		parsedRulesCache.computeIfAbsent(key, x -> new ArrayList<>()).add(stateval);
	}

	public void setProgressMonitor(ParseProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public ParseProgressMonitor getProgressMonitor() {
		return monitor;
	}

//	public void addFail(String pattern, String identifier, DocumentPosition pos) {
//		int cmp = this.pos.compareTo(pos);
//		if (cmp < 0) {
//			fails.clear();
//			this.pos = pos;
//			cmp = 0;
//		}
//		if (cmp == 0) {
//			fails.add(new ParseFail(pattern, identifier, ruleStack));
//		}
//	}

	public Set<ParseFail> getFails() {
		return fails;
	}

	@Override
	public String toString() {
		return "ParseHelper [" + (fails != null ? "fails=" + fails : "") + "]";
	}

}
