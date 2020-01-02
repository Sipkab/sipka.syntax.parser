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

import java.util.Collections;
import java.util.List;

import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.rule.Rule;

public class ParsingInformation implements Cloneable {
	private Rule rule;
	private DocumentRegion regionOfInterest;

	public ParsingInformation(Rule rule, DocumentRegion regionOfInterest) {
		this.rule = rule;
		this.regionOfInterest = regionOfInterest;
	}

	public ParsingInformation(ParsingInformation info) {
		this.rule = info.rule;
		this.regionOfInterest = new DocumentRegion(info.regionOfInterest);
	}

	@Override
	public ParsingInformation clone() {
		try {
			ParsingInformation result = (ParsingInformation) super.clone();
			result.regionOfInterest = result.regionOfInterest.clone();
			return result;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public Rule getRule() {
		return rule;
	}

	public DocumentRegion getRegionOfInterest() {
		return regionOfInterest;
	}

	public List<ParsingInformation> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return "ParsingInformation [" + (rule != null ? "rule=" + rule + ", " : "")
				+ (regionOfInterest != null ? "regionOfInterest=" + regionOfInterest : "") + "]";
	}

}
