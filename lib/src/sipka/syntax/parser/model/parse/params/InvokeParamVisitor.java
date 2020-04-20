package sipka.syntax.parser.model.parse.params;

public interface InvokeParamVisitor {
	public void visit(RegexParam param);

	public void visit(OccurrenceParam param);

	public void visit(VarReferenceParam<?> param);

	public void visit(RuleInvocationVarReferenceParam<?> param);
}
