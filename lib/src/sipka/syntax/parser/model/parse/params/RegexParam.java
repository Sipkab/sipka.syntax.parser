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
import java.util.regex.Pattern;

import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.rule.ParseHelper;

public class RegexParam implements InvokeParam<Pattern> {
	private final Pattern pattern;

	public RegexParam(Pattern pattern) {
		Objects.requireNonNull(pattern, "pattern");
		this.pattern = pattern;
	}

	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public void accept(InvokeParamVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Pattern getValue(ParseHelper helper, ParseContext context) {
		return pattern;
	}

	@Override
	public int hashCode() {
		return pattern.pattern().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegexParam other = (RegexParam) obj;
		if (!this.pattern.pattern().equals(other.pattern.pattern())) {
			return false;
		}
		if (this.pattern.flags() != other.pattern.flags()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RegexParam [pattern=" + pattern + "]";
	}

}
