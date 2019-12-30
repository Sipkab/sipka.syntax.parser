package sipka.syntax.parser.model.rule;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sipka.syntax.parser.model.parse.document.DocumentPosition;
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

	private ArrayDeque<Rule> ruleStack = new ArrayDeque<>();

	private Set<ParseFail> fails = new HashSet<>();
	private DocumentPosition pos;

	private ParseProgressMonitor monitor = ParseProgressMonitor.NULLMONITOR;

	public ParseHelper(DocumentPosition pos) {
		this.pos = pos;
	}

	public void setProgressMonitor(ParseProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public ParseProgressMonitor getProgressMonitor() {
		return monitor;
	}

	/* package */ void pushRule(Rule r) {
		ruleStack.push(r);
	}

	/* package */ void popRule() {
		ruleStack.pop();
	}

	public DocumentPosition getPosition() {
		return pos;
	}

	public void addFail(String pattern, String identifier, DocumentPosition pos) {
		int cmp = this.pos.compareTo(pos);
		if (cmp < 0) {
			fails.clear();
			this.pos = pos;
			cmp = 0;
		}
		if (cmp == 0) {
			fails.add(new ParseFail(pattern, identifier, ruleStack));
		}
	}

	public Set<ParseFail> getFails() {
		return fails;
	}

	@Override
	public String toString() {
		return "ParseHelper [" + (fails != null ? "fails=" + fails : "") + "]";
	}

}
