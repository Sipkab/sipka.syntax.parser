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
package sipka.syntax.parser.model.parse;

import sipka.syntax.parser.model.occurrence.Occurrence;
import sipka.syntax.parser.model.parse.context.DeclaringContext;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.parse.params.OccurrenceParam;

public final class ParseTimeData {
	private final InvokeParam<Occurrence> occurrenceParam;
	private final DeclaringContext declaringContext;

	public ParseTimeData(Occurrence occurrence, DeclaringContext declaringContext) {
		this.declaringContext = declaringContext;
		this.occurrenceParam = new OccurrenceParam(occurrence);
	}

	public ParseTimeData(InvokeParam<Occurrence> occurrence, DeclaringContext declaringContext) {
		this.declaringContext = declaringContext;
		this.occurrenceParam = occurrence;
	}

	public final Occurrence getOccurrence(ParseContext context) {
		return occurrenceParam.getValue(context);
	}

	public final DeclaringContext getDeclaringContext() {
		return declaringContext;
	}

	@Override
	public String toString() {
		return "ParseTimeData [" + (occurrenceParam != null ? "occurrenceParam=" + occurrenceParam : "") + "]";
	}

}