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
import sipka.syntax.parser.util.Pair;

public class ConsumedStatement extends Statement {
	private static final long serialVersionUID = -5601536902887219903L;
	private final CharSequence value;

	public ConsumedStatement(CharSequence value, DocumentRegion position) {
		super(position);
		this.value = value;
	}

	@Override
	public List<Statement> getDirectChildren() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return "ConsumedStatement [value=" + value + "]";
	}

	@Override
	public List<Statement> scopeTo(String scoper) {
		return Collections.emptyList();
	}

	@Override
	public Map<String, List<Statement>> getPossibleScopes() {
		return Collections.emptyMap();
	}

	@Override
	public List<Pair<String, Statement>> getScopes() {
		return Collections.emptyList();
	}

	@Override
	public boolean isScopesEmpty() {
		return true;
	}

	@Override
	public boolean isScopesEmpty(String scoper) {
		return true;
	}

	@Override
	protected CharSequence toValueSequence() {
		return value;
	}

	@Override
	protected CharSequence toRawSequence() {
		return value;
	}

	@Override
	public ConsumedStatement clone() {
		return (ConsumedStatement) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		ConsumedStatement other = (ConsumedStatement) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
