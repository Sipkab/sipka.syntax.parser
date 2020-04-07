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
}
