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
package sipka.syntax.parser.model.rule.consume;

import java.util.regex.Pattern;

import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.rule.RuleVisitor;

public class SkipRule extends ConsumeRule {

	public SkipRule(String identifierName, InvokeParam<Pattern> param) {
		super(identifierName, param);
	}

	@Override
	public void accept(RuleVisitor visitor) {
		visitor.visit(this);
	}

}
