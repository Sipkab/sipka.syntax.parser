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
package sipka.syntax.parser.model.occurrence;

class AtLeastOccurrence extends Occurrence {
	private final int min;

	public AtLeastOccurrence(int atleastCount) {
		this.min = atleastCount;
	}

	@Override
	public boolean isValidOccurrenceCount(int count) {
		return count >= min;
	}

	@Override
	public boolean canAcceptMore(int count) {
		return count < Integer.MAX_VALUE;
	}

	public int getAtleastCount() {
		return min;
	}

	@Override
	public String toString() {
		return "AtLeastOccurrence [min=" + min + "]";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		return Integer.MAX_VALUE - currentCount;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		if (currentCount < min) {
			return min - currentCount;
		}
		return 0;
	}

}
