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

import sipka.syntax.parser.model.rule.container.value.ValueRule.ValueConsumer;

public class CallingContext extends ParseContext {
	public static CallingContext merge(DeclaringContext contexta, ParseContext contextb) {
		CallingContext result = new CallingContext(contexta);
		result.localsMap.putAll(contextb.localsMap);
		result.valueConsumer = contextb.getCurrentValueConsumer();
		return result;
	}

	protected ValueConsumer valueConsumer;

	public CallingContext() {
	}

	public CallingContext(ParseContext context) {
		super(context);
		this.valueConsumer = context.getCurrentValueConsumer();
	}

	public CallingContext(ParseContext context, ValueConsumer consumer) {
		super(context);
		this.valueConsumer = consumer;
	}

	@Override
	public ValueConsumer getCurrentValueConsumer() {
		return valueConsumer;
	}

	public void putAllBuiltInFrom(ParseContext context) {
		context.putAllBuiltInTo(localsMap);
	}

	public void putObject(String name, Object item) {
		localsMap.put(name, item);
	}

	public void removeObject(String name) {
		localsMap.remove(name);
	}

	@Override
	public String toString() {
		return "CallingContext [localsMap=" + localsMap + "]";
	}

}
