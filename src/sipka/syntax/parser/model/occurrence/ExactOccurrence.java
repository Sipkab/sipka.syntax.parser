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

class ExactOccurrence extends Occurrence {
	private final int count;

	public ExactOccurrence(int count) {
		this.count = count;
	}

	@Override
	public boolean isValidOccurrenceCount(int count) {
		return this.count == count;
	}

	@Override
	public boolean canAcceptMore(int count) {
		return count < this.count;
	}

	@Override
	public String toString() {
		return "ExactOccurrence [count=" + count + "]";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		if (currentCount < this.count)
			return this.count - currentCount;
		return 0;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		if (currentCount < count) {
			return count - currentCount;
		}
		return 0;
	}

}
