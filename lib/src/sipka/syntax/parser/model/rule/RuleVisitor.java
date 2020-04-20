package sipka.syntax.parser.model.rule;

import sipka.syntax.parser.model.rule.consume.MatchesRule;
import sipka.syntax.parser.model.rule.consume.SkipRule;
import sipka.syntax.parser.model.rule.container.order.AnyOrderRule;
import sipka.syntax.parser.model.rule.container.order.FirstOrderRule;
import sipka.syntax.parser.model.rule.container.order.InOrderRule;
import sipka.syntax.parser.model.rule.container.value.ValueRule;
import sipka.syntax.parser.model.rule.invoke.InvokeRule;

public interface RuleVisitor {
	public void visit(MatchesRule rule);

	public void visit(SkipRule rule);

	public void visit(AnyOrderRule rule);

	public void visit(FirstOrderRule rule);

	public void visit(InOrderRule rule);

	public void visit(ValueRule rule);

	public void visit(InvokeRule rule);
}
