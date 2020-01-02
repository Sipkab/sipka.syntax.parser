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
package sipka.syntax.parser.util;

public class OffsetCharSequence implements CharSequence {
	protected CharSequence subject;
	protected int index;
	protected int length;

	public OffsetCharSequence(CharSequence subject, int index, int length) {
		this.subject = subject;
		this.index = index;
		this.length = length;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public char charAt(int index) {
		if (index < 0 || index >= length) {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index + " length: " + length);
		}
		return subject.charAt(this.index + index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		if (start < 0 || end < 0 || end > length || start > length) {
			throw new IndexOutOfBoundsException(start + " - " + end + " is not in range of 0 - " + length);
		}
		return new OffsetCharSequence(subject, this.index + start, end - start);
	}

	@Override
	public String toString() {
		return subject.subSequence(index, index + length).toString();
	}
}