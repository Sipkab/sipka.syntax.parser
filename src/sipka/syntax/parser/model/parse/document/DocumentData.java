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
package sipka.syntax.parser.model.parse.document;

import sipka.syntax.parser.util.ArrayRangeCharSequence;

public class DocumentData extends ArrayRangeCharSequence {
	public DocumentData(char[] data) {
		super(data, 0, data.length);
	}

	public DocumentData(char[] subject, int index, int length) {
		super(subject, index, length);
	}

	public DocumentData(DocumentData other) {
		super(other.array, other.index, other.length);
	}

	public void removeFromStart(int count) {
		if (count > this.length) {
			throw new IllegalArgumentException(
					"Trying to remove more than length characters: " + count + " from: " + this.length);
		}
		this.index += count;
		this.length -= count;
	}

	public ArrayRangeCharSequence subDocumentSequence(DocumentRegion region) {
		return new ArrayRangeCharSequence(array, region.getOffset(), region.getLength());
	}

	public final int getDocumentOffset() {
		return this.index;
	}

	public final int getDocumentLength() {
		return this.length;
	}

}
