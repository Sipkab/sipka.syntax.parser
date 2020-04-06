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

class RangeOccurrence extends Occurrence {

	private final int rangeStart;
	private final int rangeEnd;

	public RangeOccurrence(int rangeStart, int rangeEnd) {
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
	}

	@Override
	public boolean isValidOccurrenceCount(int count) {
		return count >= rangeStart && count <= rangeEnd;
	}

	@Override
	public boolean canAcceptMore(int count) {
		return count < rangeEnd;
	}

	@Override
	public String toString() {
		return "RangeOccurrence [rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd + "]";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		if (currentCount < rangeEnd)
			return rangeEnd - currentCount;
		return 0;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		if (currentCount < rangeStart) {
			return rangeEnd - currentCount;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + rangeEnd;
		result = prime * result + rangeStart;
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
		RangeOccurrence other = (RangeOccurrence) obj;
		if (rangeEnd != other.rangeEnd)
			return false;
		if (rangeStart != other.rangeStart)
			return false;
		return true;
	}
}
