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
//		if (contexta.localsMap.isEmpty()) {
//			return contextb;
//		}
//		if (contextb.localsMap.isEmpty()) {
//			return new CallingContext(contexta.localsMap, contextb.getCurrentValueConsumer());
//		}
//		NavigableMap<String, Object> localsMap;
//		localsMap = new TreeMap<>(contexta.localsMap);
//		localsMap.putAll(contextb.localsMap);
//		return new CallingContext(localsMap, contextb.getCurrentValueConsumer());
	}

	public static ParseContext mergeWithBuiltInsAndVariables(ParseContext contexta, DeclaringContext contextb,
			NavigableMap<String, Object> vars) {
		return new ParseContext(new ParseContext(contexta, 0, contextb.localsMap), FLAG_PARENT_BUILTINS_ONLY, vars);
//		if (vars.isEmpty()) {
//			//we can avoid creating a new Map if there are no variables and no builtins
//			NavigableMap<String, Object> localmap = null;
//			for (Entry<String, Object> entry : contextb.localsMap.entrySet()) {
//				String key = entry.getKey();
//				if (key.startsWith(Rule.BUILTIN_VAR_PREFIX)) {
//					if (localmap == null) {
//						localmap = new TreeMap<>(contexta.localsMap);
//					}
//					localmap.put(key, entry.getValue());
//				}
//			}
//			if (localmap == null) {
//				localmap = contexta.localsMap;
//			}
//			CallingContext result = new CallingContext(localmap, contextb.getCurrentValueConsumer());
//			return result;
//		}
//		CallingContext result = new CallingContext(new TreeMap<>(contexta.localsMap),
//				contextb.getCurrentValueConsumer());
//		contextb.putAllBuiltInTo(result.localsMap);
//		result.localsMap.putAll(vars);
//		return result;
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
