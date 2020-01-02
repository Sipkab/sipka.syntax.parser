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
package sipka.syntax.parser.model.statement.repair;

import java.util.ArrayList;
import java.util.List;

import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.rule.Rule;

public class CollectionParsingInformation extends ParsingInformation {
	private List<ParsingInformation> children;

	public CollectionParsingInformation(Rule rule, DocumentRegion regionOfInterest, List<ParsingInformation> children) {
		super(rule, regionOfInterest);
		this.children = children;
	}

	public CollectionParsingInformation(ParsingInformation info, List<ParsingInformation> children) {
		super(info);
		this.children = children;
	}

	@Override
	public CollectionParsingInformation clone() {
		CollectionParsingInformation result = (CollectionParsingInformation) super.clone();
		result.children = new ArrayList<>();
		for (ParsingInformation info : this.children) {
			result.children.add(info.clone());
		}
		return result;
	}

	@Override
	public List<ParsingInformation> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "CollectionParsingInformation [" + (children != null ? "children=" + children : "") + "]";
	}

}
