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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	private Set<ParseFail> fails = new HashSet<>();

	private ParseProgressMonitor monitor = ParseProgressMonitor.NULLMONITOR;

	public ParseHelper() {
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
