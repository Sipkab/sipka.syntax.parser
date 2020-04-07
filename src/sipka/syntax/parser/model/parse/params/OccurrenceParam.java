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
package sipka.syntax.parser.model.parse.params;

import java.util.Objects;

import sipka.syntax.parser.model.ParseFailedException;
import sipka.syntax.parser.model.occurrence.Occurrence;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.rule.ParseHelper;

public class OccurrenceParam implements InvokeParam<Occurrence> {
	private final Occurrence occurrence;

	public OccurrenceParam(String occurrenceString) throws ParseFailedException {
		this.occurrence = Occurrence.parse(occurrenceString);
	}

	public OccurrenceParam(Occurrence occurrence) {
		Objects.requireNonNull(occurrence, "occurrence");
		this.occurrence = occurrence;
	}

	@Override
	public Occurrence getValue(ParseHelper helper, ParseContext context) {
		return occurrence;
	}

	@Override
	public String toString() {
		return "OccurrenceParam [occurrence=" + occurrence + "]";
	}

	@Override
	public int hashCode() {
		return occurrence.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OccurrenceParam other = (OccurrenceParam) obj;
		if (!occurrence.equals(other.occurrence))
			return false;
		return true;
	}

}
