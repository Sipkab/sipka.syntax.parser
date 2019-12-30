package sipka.syntax.parser.model.statement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.util.Pair;

public class ValueStatement extends Statement {
	private static final long serialVersionUID = -1399545866829166300L;

	protected CharSequence value;
	protected Statement subStatement;

	public ValueStatement(String name, CharSequence value, Statement substatement) {
		super(name, new DocumentRegion(substatement.getPosition()));
		this.value = value;
		this.subStatement = substatement;
	}

	public Statement getSubStatement() {
		return subStatement;
	}

	@Override
	public List<Statement> getDirectChildren() {
		return Collections.singletonList(subStatement);
	}

	@Override
	public String toString() {
		return "ValueStatement [getName()=" + getName() + ", value=" + value + "]";
	}

	@Override
	public List<Statement> scopeTo(String scoper) {
		return subStatement.scopeTo(scoper);
	}

	@Override
	public Map<String, List<Statement>> getPossibleScopes() {
		return subStatement.getPossibleScopes();
	}

	@Override
	public List<Pair<String, Statement>> getScopes() {
		return subStatement.getScopes();
	}

	@Override
	public boolean isScopesEmpty() {
		return subStatement.isScopesEmpty();
	}

	@Override
	public boolean isScopesEmpty(String scoper) {
		return subStatement.isScopesEmpty(scoper);
	}

	@Override
	protected CharSequence toValueSequence() {
		return value;
	}

	@Override
	protected CharSequence toRawSequence() {
		return subStatement.toRawSequence();
	}

	@Override
	public ValueStatement clone() {
		ValueStatement result = (ValueStatement) super.clone();
		result.subStatement = this.subStatement.clone();
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((subStatement == null) ? 0 : subStatement.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueStatement other = (ValueStatement) obj;
		if (subStatement == null) {
			if (other.subStatement != null)
				return false;
		} else if (!subStatement.equals(other.subStatement))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
