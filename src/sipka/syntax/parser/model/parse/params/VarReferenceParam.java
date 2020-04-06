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

import sipka.syntax.parser.model.parse.context.ParseContext;

public class VarReferenceParam<T> implements InvokeParam<T> {
	private final String variableName;

	public VarReferenceParam(String varname) {
		this.variableName = varname;
	}

	public final String getVarname() {
		return variableName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue(ParseContext context) {
		try {
			Object result = context.getObjectForName(variableName);
			while (result instanceof InvokeParam<?>) {
				result = ((InvokeParam<?>) result).getValue(context);
			}
			return (T) result;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Failed to cast variable reference parameter ", e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
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
		VarReferenceParam<?> other = (VarReferenceParam<?>) obj;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		} else if (!variableName.equals(other.variableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VarReferenceParam [varname=" + variableName + "]";
	}

}
