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
package sipka.syntax.parser.model.statement.modifiable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.util.Pair;

public final class ModifiableStatement implements Iterable<ModifiableStatement> {
	private static final String ROOT_STM_NAME = "@root_modifiable_stm";

	public static ModifiableStatement parse(Statement start) {
		return new ModifiableStatement(start);
	}

	private final List<ModifiableStatement> children = new ArrayList<>();
	private String name;
	private String value;
	private ModifiableStatement parent;

	private ModifiableStatement(Statement startstm) {
		this.name = ROOT_STM_NAME;
		this.value = "";
		this.parent = null;

		addChildren(this, startstm);
	}

	private ModifiableStatement(String name, ModifiableStatement parent, Statement stm) {
		this.name = name;
		this.value = stm.getValue();
		this.parent = parent;

		addChildren(this, stm);
	}

	public ModifiableStatement(String name, String value, ModifiableStatement parent) {
		this.name = name;
		this.value = value;
		this.parent = parent;
	}

	private void addChildren(ModifiableStatement parent, Statement stm) {
		for (Pair<String, Statement> pair : stm.getScopes()) {
			ModifiableStatement ms = new ModifiableStatement(pair.key, parent, pair.value);
			children.add(ms);
		}
	}

	public final void printHierarchy() {
		prettyprint(this, 0);
	}

	private static void prettyprint(ModifiableStatement stm, int tabcount) {
		for (ModifiableStatement ms : stm) {
			for (int i = 0; i < tabcount; i++) {
				System.out.print("|\t");
			}
			System.out.println(ms.getName() + ": " + ms.getValue()/* + " " + ms.getParent() */);
			prettyprint(ms, tabcount + 1);
		}
	}

	public final String getName() {
		return name;
	}

	public final String getValue() {
		return value;
	}

	public final ModifiableStatement getParent() {
		return parent;
	}

	public final void add(ModifiableStatement e) {
		if (e == null) {
			return;
		}

		if (e.parent != null) {
			e.parent.removeChild(e);
		}
		e.parent = this;
		children.add(e);
	}

	public final void add(int index, ModifiableStatement e) {
		if (e == null) {
			return;
		}
		if (e.parent != null) {
			e.parent.removeChild(e);
		}
		e.parent = this;
		children.add(index, e);
	}

	public final void insertAfter(ModifiableStatement child, ModifiableStatement toinsert) {
		add(indexOfChild(child) + 1, toinsert);
	}

	public final void insertBefore(ModifiableStatement child, ModifiableStatement toinsert) {
		add(indexOfChild(child), toinsert);
	}

	public final void addChildren(List<ModifiableStatement> children) {
		ArrayList<ModifiableStatement> list = new ArrayList<>(children);
		for (ModifiableStatement c : list) {
			add(c);
		}
	}

	public boolean hasChildNamed(String name) {
		for (ModifiableStatement ms : children) {
			if (ms.name.equals(name))
				return true;
		}
		return false;
	}

	public boolean hasParentNamed(String name) {
		if (parent == null)
			return false;
		return parent.name.equals(name);
	}

	public boolean hasDescendantNamed(String name) {
		for (ModifiableStatement ms : children) {
			if (ms.name.equals(name))
				return true;

			if (ms.hasDescendantNamed(name))
				return true;
		}
		return false;
	}

	public boolean hasAncestorNamed(String name) {
		if (parent == null)
			return false;
		if (parent.getName().equals(name))
			return true;

		return parent.hasAncestorNamed(name);
	}

	public boolean hasSiblingNamed(String name) {
		if (parent == null)
			return false;
		for (ModifiableStatement sib : parent.children) {
			if (sib == this)
				continue;
			if (sib.name.equals(name))
				return true;
		}
		return false;
	}

	public boolean hasRelationNamed(TreeRelation relation, String name) {
		switch (relation) {
			case ANCESTOR:
				return hasAncestorNamed(name);
			case CHILD:
				return hasChildNamed(name);
			case DESCENDANT:
				return hasDescendantNamed(name);
			case PARENT:
				return hasParentNamed(name);
			case SIBLING:
				return hasSiblingNamed(name);
			default:
				return false;
		}
	}

	public ModifiableStatement getRelationNamed(TreeRelation relation, String name) {
		switch (relation) {
			case ANCESTOR:
				return getAncestorNamed(name);
			case CHILD:
				return getChildNamed(name);
			case DESCENDANT:
				return getDescendantNamed(name);
			case PARENT:
				return getParentNamed(name);
			case SIBLING:
				return getSiblingNamed(name);
			default:
				return null;
		}
	}

	public ModifiableStatement getSiblingNamed(String name) {
		if (parent == null)
			return null;
		for (ModifiableStatement sib : parent.children) {
			if (sib == this)
				continue;
			if (sib.name.equals(name))
				return sib;
		}
		return null;
	}

	public ModifiableStatement getParentNamed(String name) {
		if (parent == null)
			return null;

		return parent.name.equals(name) ? parent : null;
	}

	public ModifiableStatement getDescendantNamed(String name) {
		for (ModifiableStatement ms : children) {
			if (ms.name.equals(name))
				return ms;

			ModifiableStatement desc = ms.getDescendantNamed(name);
			if (desc != null) {
				return desc;
			}
		}
		return null;
	}

	public ModifiableStatement getChildNamed(String name) {
		for (ModifiableStatement ms : children) {
			if (ms.name.equals(name))
				return ms;
		}
		return null;
	}

	public String getChildValue(String childName) {
		for (ModifiableStatement ms : children) {
			if (ms.name.equals(childName))
				return ms.getValue();
		}
		return null;
	}

	public List<ModifiableStatement> getChildrenNamed(String name) {
		List<ModifiableStatement> result = new ArrayList<>();
		for (ModifiableStatement ms : children) {
			if (ms.name.equals(name))
				result.add(ms);
		}
		return result;
	}

	public ModifiableStatement getAncestorNamed(String name) {
		if (parent == null)
			return null;
		if (parent.getName().equals(name))
			return parent;

		return parent.getAncestorNamed(name);
	}

	@Override
	public Iterator<ModifiableStatement> iterator() {
		return children.iterator();
	}

	public List<ModifiableStatement> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "ModifiableStatement [" + (!"".equals(name) ? "name=" + name + ", " : "")
				+ (!"".equals(value) ? "value=" + value : "") + "]";
	}

	public boolean removeChild(ModifiableStatement stm) {
		boolean remove = children.remove(stm);
		if (remove) {
			stm.parent = null;
		}
		return remove;
	}

	public boolean replaceChild(ModifiableStatement oldStm, ModifiableStatement newStm) {
		int index = children.indexOf(oldStm);
		if (index < 0)
			return false;

		oldStm.parent = null;
		newStm.parent = this;

		children.set(index, newStm);
		return true;
	}

	public void swapWith(ModifiableStatement target) {
		if (this.parent == null) {
			if (target.parent == null)
				return;
			target.parent.replaceChild(target, this);
		} else {
			if (target.parent == null) {
				this.parent.replaceChild(this, target);
			} else {
				this.parent.children.set(this.parent.children.indexOf(this), target);
				target.parent.children.set(target.parent.children.indexOf(target), this);

				ModifiableStatement myparent = this.parent;
				this.parent = target.parent;
				target.parent = myparent;

			}
		}
	}

	public int indexOfChild(ModifiableStatement stm) {
		return children.indexOf(stm);
	}

	public void removeChildAt(int index) {
		ModifiableStatement removed = children.remove(index);
		removed.parent = null;
	}

	public void replaceChildAt(int index, ModifiableStatement stm) {
		replaceChild(children.get(index), stm);
	}

	public void removeFirstChildWithName(String name) {
		for (Iterator<ModifiableStatement> it = children.iterator(); it.hasNext();) {
			ModifiableStatement child = it.next();
			if (child.getName().equals(name)) {
				it.remove();
				break;
			}
		}
	}

	public void removeChildrenWithName(String name) {
		for (Iterator<ModifiableStatement> it = children.iterator(); it.hasNext();) {
			ModifiableStatement child = it.next();
			if (child.getName().equals(name)) {
				it.remove();
			}
		}
	}

	public ModifiableStatement getChildAt(int index) {
		return children.get(index);
	}

	public int getChildCount() {
		return children.size();
	}

	public boolean hasNextSibling() {
		return parent != null && parent.indexOfChild(this) < parent.getChildCount() - 1;
	}

	public boolean hasPreviousSibling() {
		return parent != null && parent.indexOfChild(this) > 0;
	}

	public ModifiableStatement getNextSibling() {
		if (parent == null)
			return null;
		int index = parent.indexOfChild(this);
		return parent.getChildAt(index + 1);
	}

	public ModifiableStatement getPreviousSibling() {
		if (parent == null)
			return null;
		int index = parent.indexOfChild(this);
		return parent.getChildAt(index - 1);
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final void setValue(String value) {
		this.value = value;
	}
}
