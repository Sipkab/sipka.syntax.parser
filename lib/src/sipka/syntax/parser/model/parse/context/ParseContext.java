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

import java.util.Collection;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

import sipka.syntax.parser.model.rule.Rule;
import sipka.syntax.parser.model.rule.container.value.ValueConsumer;
import sipka.syntax.parser.util.Pair;

public class ParseContext {
	public static final ParseContext EMPTY = new ParseContext(Collections.emptyNavigableMap());

	public static final int FLAG_PARENT_BUILTINS_ONLY = 1 << 0;

	protected final ParseContext parent;
	protected final int parentFlags;
	protected final NavigableMap<String, Object> localsMap;

	protected ParseContext(ParseContext parent, int parentflags, NavigableMap<String, Object> localsMap) {
		this.parent = parent;
		this.parentFlags = parentflags;
		this.localsMap = localsMap;
	}

	protected ParseContext(NavigableMap<String, Object> localsMap) {
		this(null, 0, localsMap);
	}

	public ParseContext(Collection<Pair<String, Object>> locals) throws IllegalArgumentException {
		this(toLocalsMap(locals));
	}

	private static NavigableMap<String, Object> toLocalsMap(Collection<Pair<String, Object>> locals)
			throws IllegalArgumentException {
		if (locals.isEmpty()) {
			return Collections.emptyNavigableMap();
		}
		TreeMap<String, Object> initlocals = new TreeMap<>();
		for (Pair<String, Object> local : locals) {
			Object prev = initlocals.put(local.key, local.value);
			if (prev != null) {
				throw new IllegalArgumentException("Duplicate variables in scope with name: " + local.key);
			}
		}
		return initlocals;
	}

	public ValueConsumer getCurrentValueConsumer() {
		if (parent != null) {
			return parent.getCurrentValueConsumer();
		}
		return null;
	}

	public Object getObjectForName(String name) {
		return getObjectForName(name, null);
	}

	public Object getObjectForName(String name, Object defaultValue) {
		Object result = localsMap.get(name);
		if (result != null) {
			return result;
		}
		if (parent != null) {
			if (((parentFlags & FLAG_PARENT_BUILTINS_ONLY) == FLAG_PARENT_BUILTINS_ONLY)) {
				if (!(name.startsWith(Rule.BUILTIN_VAR_PREFIX))) {
					return defaultValue;
				}
			}
			return parent.getObjectForName(name, defaultValue);
		}
		return defaultValue;
	}

	public ParseContext getParent() {
		return parent;
	}

	public NavigableMap<String, Object> getLocalsMap() {
		return localsMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((localsMap == null) ? 0 : localsMap.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + parentFlags;
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
		ParseContext other = (ParseContext) obj;
		if (localsMap == null) {
			if (other.localsMap != null)
				return false;
		} else if (!localsMap.equals(other.localsMap))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (parentFlags != other.parentFlags)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParseContext [localsMap=" + localsMap + "]";
	}

}
