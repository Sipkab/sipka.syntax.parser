package sipka.syntax.parser.model.rule;

import java.util.Objects;

import sipka.syntax.parser.model.parse.context.DeclaringContext;

public final class RuleDeclaration {
	private final Rule rule;
	private DeclaringContext declarationContext;

	public RuleDeclaration(Rule rule) {
		this(rule, null);
	}

	public RuleDeclaration(Rule rule, DeclaringContext declarationContext) {
		Objects.requireNonNull(rule, "rule");
		this.rule = rule;
		this.declarationContext = declarationContext;
	}

	public Rule getRule() {
		return rule;
	}

	public DeclaringContext getDeclarationContext() {
		return declarationContext;
	}

	public void setDeclarationContext(DeclaringContext declarationContext) {
		this.declarationContext = declarationContext;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
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
		RuleDeclaration other = (RuleDeclaration) obj;
		if (declarationContext == null) {
			if (other.declarationContext != null)
				return false;
		} else if (!declarationContext.equals(other.declarationContext))
			return false;
		if (rule == null) {
			if (other.rule != null)
				return false;
		} else if (!rule.equals(other.rule))
			return false;
		return true;
	}

}
