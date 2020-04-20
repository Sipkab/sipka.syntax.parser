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
package sipka.syntax.parser.model.parse.context;

import java.util.NavigableMap;

import sipka.syntax.parser.model.rule.container.value.ValueConsumer;

public class CallingContext extends ParseContext {
	public static ParseContext merge(DeclaringContext contexta, ParseContext contextb) {
		return new CallingContext(contexta, 0, contextb.localsMap, contextb.getCurrentValueConsumer());
	}

	public static ParseContext mergeWithBuiltInsAndVariables(ParseContext contexta, DeclaringContext contextb,
			NavigableMap<String, Object> vars) {
		return new ParseContext(new ParseContext(contexta, 0, contextb.localsMap), FLAG_PARENT_BUILTINS_ONLY, vars);
	}

	protected transient ValueConsumer valueConsumer;

	public CallingContext(ParseContext parent, int parentflags, NavigableMap<String, Object> localsMap,
			ValueConsumer valueConsumer) {
		super(parent, parentflags, localsMap);
		this.valueConsumer = valueConsumer;
	}

	protected CallingContext(NavigableMap<String, Object> localsMap, ValueConsumer valueConsumer) {
		super(localsMap);
		this.valueConsumer = valueConsumer;
	}

	public CallingContext(ParseContext context, ValueConsumer consumer) {
		super(context.localsMap);
		this.valueConsumer = consumer;
	}

	@Override
	public ValueConsumer getCurrentValueConsumer() {
		return valueConsumer;
	}

	@Override
	public String toString() {
		return "CallingContext [localsMap=" + localsMap + "]";
	}

}
