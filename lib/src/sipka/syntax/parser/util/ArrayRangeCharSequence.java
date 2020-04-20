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

public class ArrayRangeCharSequence implements CharSequence {
	public static final ArrayRangeCharSequence EMPTY = new ArrayRangeCharSequence(new char[0], 0, 0);

	protected final char[] array;
	protected int index;
	protected int length;

	public ArrayRangeCharSequence(char[] subject) {
		this(subject, 0, subject.length);
	}

	public ArrayRangeCharSequence(char[] subject, int index, int length) {
		this.array = subject;
		this.index = index;
		this.length = length;
	}

	public static ArrayRangeCharSequence valueOf(String str) {
		return new ArrayRangeCharSequence(str.toCharArray());
	}

	public static ArrayRangeCharSequence valueOf(CharSequence str) {
		if (str instanceof String) {
			return valueOf((String) str);
		}
		char[] array = sequenceToArray(str);
		return new ArrayRangeCharSequence(array);
	}

	public static char[] sequenceToArray(CharSequence str) {
		char[] array = new char[str.length()];
		for (int i = 0; i < array.length; i++) {
			array[i] = str.charAt(i);
		}
		return array;
	}

	public char[] array() {
		return array;
	}

	public int index() {
		return index;
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
		return array[this.index + index];
	}

	@Override
	public ArrayRangeCharSequence subSequence(int start, int end) {
		if (start < 0 || end < 0 || end > length || start > length) {
			throw new IndexOutOfBoundsException(start + " - " + end + " is not in range of 0 - " + length);
		}
		return new ArrayRangeCharSequence(array, this.index + start, end - start);
	}

	@Override
	public String toString() {
		return String.valueOf(array, index, length);
	}
}