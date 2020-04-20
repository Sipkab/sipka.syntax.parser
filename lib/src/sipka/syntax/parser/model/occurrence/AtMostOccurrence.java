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

class AtMostOccurrence extends Occurrence {
	private final int max;

	public AtMostOccurrence(int max) {
		this.max = max;
	}

	@Override
	public boolean isValidOccurrenceCount(int count) {
		return count <= max;
	}

	@Override
	public boolean canAcceptMore(int count) {
		return count < max;
	}

	public int getMax() {
		return max;
	}

	@Override
	public String toString() {
		if (max == 1) {
			return "?";
		}
		return max + "-";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		if (max > currentCount)
			return max - currentCount;
		return 0;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + max;
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
		AtMostOccurrence other = (AtMostOccurrence) obj;
		if (max != other.max)
			return false;
		return true;
	}

}
