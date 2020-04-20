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
package sipka.syntax.parser.model.rule.container.value;

import java.util.Arrays;

import sipka.syntax.parser.util.ArrayRangeCharSequence;

public class ValueConsumer {
	private static final char[] EMPTY_CHAR_ARRAY = new char[0];

	private char[] array = EMPTY_CHAR_ARRAY;
	private int count = 0;

	public void appendValue(char[] chars, int index, int length) {
		if (length == 0) {
			return;
		}
		ensureCount(length);
		System.arraycopy(chars, index, this.array, count, length);
		count += length;
	}

	public void appendValue(ArrayRangeCharSequence parsed) {
		appendValue(parsed.array(), parsed.index(), parsed.length());
	}

	public void appendValue(CharSequence parsed) {
		if (parsed instanceof ArrayRangeCharSequence) {
			appendValue((ArrayRangeCharSequence) parsed);
		} else if (parsed instanceof String) {
			appendValue((String) parsed);
		} else {
			int length = parsed.length();
			if (length == 0) {
				return;
			}
			ensureCount(length);
			for (int i = 0; i < length; i++) {
				this.array[this.count++] = parsed.charAt(i);
			}
		}
	}

	public void appendValue(String str) {
		int length = str.length();
		if (length == 0) {
			return;
		}
		ensureCount(length);
		str.getChars(0, length, array, count);
		count += length;
	}

	protected ArrayRangeCharSequence getParsedValue() {
		return new ArrayRangeCharSequence(array, 0, count);
	}

	public int length() {
		return count;
	}

	public void setLength(int len) {
		count = len;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + String.copyValueOf(array, 0, count) + "]";
	}

	private void ensureCount(int length) {
		if (count + length > array.length) {
			int nlen = Math.max(Integer.highestOneBit(count + length) << 1, 16);
			this.array = Arrays.copyOf(array, nlen);
		}
	}

}