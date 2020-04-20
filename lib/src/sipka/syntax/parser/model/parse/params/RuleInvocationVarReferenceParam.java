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
import sipka.syntax.parser.model.rule.Rule;

public class RuleInvocationVarReferenceParam<T> implements InvokeParam<T> {
	private final Rule rule;
	private final String variableName;

	public RuleInvocationVarReferenceParam(Rule rule, String variableName) {
		Objects.requireNonNull(rule, "rule");
		Objects.requireNonNull(variableName, "variable name");
		this.rule = rule;
		this.variableName = variableName;
	}

	@Override
	public void accept(InvokeParamVisitor visitor) {
		visitor.visit(this);
	}

	public Rule getRule() {
		return rule;
	}

	public final String getVariableName() {
		return variableName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue(ParseHelper helper, ParseContext context) {
		Object result = context.getObjectForName(rule.createParameterName(variableName));
		while (result instanceof InvokeParam<?>) {
			result = ((InvokeParam<?>) result).getValue(helper, context);
		}
		return (T) result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
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
		RuleInvocationVarReferenceParam<?> other = (RuleInvocationVarReferenceParam<?>) obj;
		if (rule == null) {
			if (other.rule != null)
				return false;
		} else if (!rule.equals(other.rule))
			return false;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		} else if (!variableName.equals(other.variableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RuleInvocationVarReferenceParam[rule=" + rule + ", variableName=" + variableName + "]";
	}

}
