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
package sipka.syntax.parser.model.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public class CollectionStatement extends Statement implements Iterable<Statement> {
	private static final long serialVersionUID = 7340231830076247982L;

	public static final class Builder {
		private final List<ParsingResult> children = new ArrayList<>();

		public ParsingResult build(DocumentData s, DocumentRegion position, ParsingInformation parsinginformation) {
			List<Statement> stms = getStatements();
			return new ParsingResult(new CollectionStatement(s.subDocumentSequence(position), position, stms),
					parsinginformation);
		}

		public ParsingResult fail(ParsingInformation parsinginformation) {
			return new ParsingResult(null, parsinginformation);
		}

		public List<Statement> getStatements() {
			List<Statement> result = new ArrayList<>();
			for (ParsingResult pr : children) {
				result.add(pr.getStatement());
			}
			return result;
		}

		public List<ParsingInformation> getParsingInformations() {
			List<ParsingInformation> result = new ArrayList<>();
			for (ParsingResult pr : children) {
				result.add(pr.getParsingInformation());
			}
			return result;
		}

		public void add(ParsingResult stm) {
			children.add(stm);
		}

		public void clear() {
			children.clear();
		}

		public int size() {
			return children.size();
		}
	}

	protected List<Statement> children;
	private CharSequence rawValue;

	public CollectionStatement(CharSequence rawvalue, DocumentRegion position, List<Statement> children) {
		super(position);
		this.children = children;
		this.rawValue = rawvalue;
	}

	@Override
	public List<Statement> getDirectChildren() {
		return children;
	}

	@Override
	public List<Statement> scopeTo(String scoper) {
		List<Statement> result = new ArrayList<>();

		for (Statement stm : children) {
			String stmname = stm.getName();
			if (stmname.isEmpty()) {
				result.addAll(stm.scopeTo(scoper));
			} else if (scoper.equals(stmname)) {
				result.add(stm);
			}
		}
		return result;
	}

	@Override
	public Map<String, List<Statement>> getPossibleScopes() {
		Map<String, List<Statement>> result = new TreeMap<>();
		for (Statement stm : children) {
			String stmname = stm.getName();
			if (stmname.isEmpty()) {
				Map<String, List<Statement>> possible = stm.getPossibleScopes();
				for (Entry<String, List<Statement>> entry : possible.entrySet()) {
					String scoper = entry.getKey();
					List<Statement> list = result.computeIfAbsent(scoper, k -> new ArrayList<>());
					list.addAll(entry.getValue());
				}
			} else {
				result.put(stmname, Arrays.asList(stm));
			}
		}
		return result;
	}

	@Override
	public List<Pair<String, Statement>> getScopes() {
		List<Pair<String, Statement>> result = new ArrayList<>();

		for (Statement stm : children) {
			String stmname = stm.getName();
			if (stmname.isEmpty()) {
				result.addAll(stm.getScopes());
			} else {
				result.add(new Pair<>(stmname, stm));
			}
		}

		return result;
	}

	@Override
	public boolean isScopesEmpty() {
		for (Statement stm : children) {
			if (!stm.getName().isEmpty()) {
				return false;
			}
			if (!stm.isScopesEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isScopesEmpty(String scoper) {
		for (Statement stm : children) {
			String stmname = stm.getName();
			if (scoper.equals(stmname)) {
				return false;
			}
			if (stmname.isEmpty()) {
				if (!stm.isScopesEmpty(scoper)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected CharSequence toValueSequence() {
		return "";
	}

	@Override
	protected CharSequence toRawSequence() {
		return rawValue;
	}

	@Override
	public Iterator<Statement> iterator() {
		return children.iterator();
	}

	@Override
	public CollectionStatement clone() {
		CollectionStatement result = (CollectionStatement) super.clone();
		result.children = new ArrayList<>();
		for (Statement c : this.children) {
			result.children.add(c.clone());
		}
		return result;
	}

	@Override
	public String toString() {
		return "CollectionStatement [children=" + children.size() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CollectionStatement other = (CollectionStatement) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		return true;
	}

}
