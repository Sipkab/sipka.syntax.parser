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
package sipka.syntax.parser.model.manipulate;

import java.util.Iterator;
import java.util.Stack;

public class ModifiableStatementTreeWalker implements Iterator<ModifiableStatement> {
	private final ModifiableStatement root;

	private ModifiableStatement next;
	private Stack<Iterator<ModifiableStatement>> childrenStack = new Stack<>();

	public ModifiableStatementTreeWalker(ModifiableStatement root) {
		this.root = root;
		reset();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public ModifiableStatement next() {
		final ModifiableStatement result = next;
		// get next

		while (!childrenStack.empty() && !childrenStack.peek().hasNext()) {
			childrenStack.pop();
		}
		if (childrenStack.empty()) {
			next = null;
		} else {
			next = childrenStack.peek().next();
			childrenStack.push(next.iterator());
		}

		return result;
	}

	public void reset() {
		next = root;
		childrenStack.clear();
		childrenStack.push(next.iterator());
	}

}
