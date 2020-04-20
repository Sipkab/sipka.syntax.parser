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

import sipka.syntax.parser.util.Pair;

public class DeclaringContext extends ParseContext {
	public static final DeclaringContext EMPTY = new DeclaringContext(Collections.emptyNavigableMap());

	private DeclaringContext(NavigableMap<String, Object> localsMap) {
		super(localsMap);
	}

	public DeclaringContext(Collection<Pair<String, Object>> locals) throws IllegalArgumentException {
		super(locals);
	}

	@Override
	public String toString() {
		return "DeclaringContext [localsMap=" + localsMap + "]";
	}

}
