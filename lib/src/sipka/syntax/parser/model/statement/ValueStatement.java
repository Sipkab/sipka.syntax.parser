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
package sipka.syntax.parser.model.statement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.util.ArrayRangeCharSequence;
import sipka.syntax.parser.util.Pair;

public class ValueStatement extends Statement {
	private static final long serialVersionUID = -1399545866829166300L;

	protected String name;
	protected ArrayRangeCharSequence value;
	protected Statement subStatement;

	public ValueStatement(String name, ArrayRangeCharSequence value, Statement substatement) {
		super(new DocumentRegion(substatement.getPosition()));
		this.name = name;
		this.value = value;
		this.subStatement = substatement;
	}

	public ValueStatement withName(String name) {
		ValueStatement result = this.clone();
		result.name = name;
		return result;
	}

	public Statement getSubStatement() {
		return subStatement;
	}

	@Override
	public String getName() {
		return name;
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
	protected ArrayRangeCharSequence toValueSequence() {
		return value;
	}

	@Override
	protected ArrayRangeCharSequence toRawSequence() {
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
		if (!this.name.equals(other.name)) {
			return false;
		}
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
