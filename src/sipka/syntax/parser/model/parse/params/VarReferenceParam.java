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
package sipka.syntax.parser.model.parse.params;

import java.util.Objects;

import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.rule.ParseHelper;

public class VarReferenceParam<T> implements InvokeParam<T> {
	private final String variableName;

	public VarReferenceParam(String varname) {
		Objects.requireNonNull(varname, "variable name");
		this.variableName = varname;
	}

	public final String getVarname() {
		return variableName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue(ParseHelper helper, ParseContext context) {
		Object result = context.getObjectForName(variableName);
		while (result instanceof InvokeParam<?>) {
			result = ((InvokeParam<?>) result).getValue(helper, context);
		}
		return (T) result;
	}

	@Override
	public int hashCode() {
		return variableName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VarReferenceParam<?> other = (VarReferenceParam<?>) obj;
		if (!variableName.equals(other.variableName)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "VarReferenceParam [varname=" + variableName + "]";
	}

}
