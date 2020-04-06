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

import java.util.ArrayList;
import java.util.List;

class MultipleOccurrence extends Occurrence {

	private List<Occurrence> occurs = new ArrayList<>();

	@Override
	public boolean isValidOccurrenceCount(int count) {
		for (Occurrence rule : occurs) {
			if (rule.isValidOccurrenceCount(count))
				return true;
		}
		return false;
	}

	@Override
	public boolean canAcceptMore(int count) {
		for (Occurrence rule : occurs) {
			if (rule.canAcceptMore(count))
				return true;
		}
		return false;
	}

	public void add(Occurrence rule) {
		occurs.add(rule);
	}

	@Override
	public String toString() {
		return "MultipleOccurence [occurs=" + occurs + "]";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		int max = 0;
		for (Occurrence rule : occurs) {
			int c = rule.getSafelyAddCount(currentCount);
			if (c > max)
				max = c;
		}
		return max;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		int min = -1;
		for (Occurrence rule : occurs) {
			int req = rule.getRequiredMoreCount(currentCount);
			if (min < 0) {
				min = req;
			} else {
				min = Math.min(min, req);
			}
		}
		return min < 0 ? 0 : min;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((occurs == null) ? 0 : occurs.hashCode());
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
		MultipleOccurrence other = (MultipleOccurrence) obj;
		if (occurs == null) {
			if (other.occurs != null)
				return false;
		} else if (!occurs.equals(other.occurs))
			return false;
		return true;
	}
}
