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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class DocumentRegion implements Cloneable, Externalizable {
	private static final long serialVersionUID = 1L;

	protected int offset;
	protected int length;

	public DocumentRegion() {
	}

	public DocumentRegion(int offset, int length) {
		set(offset, length);
	}

	public DocumentRegion(DocumentRegion o) {
		this.offset = o.offset;
		this.length = o.length;
	}

	@Override
	public DocumentRegion clone() {
		try {
			return (DocumentRegion) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError("Cloning failed.", e);
		}
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public void setOffset(int offset) {
		if (offset < 0) {
			throw new IllegalArgumentException("Offset must be positive: " + offset);
		}
		this.offset = offset;
	}

	public void expandTo(DocumentRegion region) {
		expandTo(region.offset, region.length);
	}

	public void expandTo(int offset, int length) {
		if (this.offset > offset) {
			int diff = this.offset - offset;
			this.offset = offset;
			this.length += diff;
		}
		if (offset + length > this.getEndOffset()) {
			this.length = offset + length - this.offset;
		}
	}

	public void setLength(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("Length must be positive: " + length);
		}
		this.length = length;
	}

	public void set(int offset, int length) {
		if (length < 0) {
			throw new IllegalArgumentException("Length must be positive: " + length);
		}
		if (offset < 0) {
			throw new IllegalArgumentException("Offset must be positive: " + offset);
		}
		this.offset = offset;
		this.length = length;
	}

	/**
	 * Returns the offset where this region ends (exclusive). The ending offset can be equal the start offset when the
	 * length is zero.
	 * 
	 * @return The ending offset.
	 */
	public int getEndOffset() {
		return offset + length;
	}

	public boolean isInside(int offset) {
		return offset >= this.offset && offset < this.offset + length;
	}

	public boolean isInside(DocumentRegion region) {
		return isInside(region.getOffset(), region.getLength());
	}

	public boolean isInside(int offset, int length) {
		return offset >= this.getOffset() && offset + length <= this.getEndOffset();
	}

	public boolean isOverlapping(DocumentRegion region) {
		if (region.isEmpty()) {
			return false;
		}
		return isInside(getOffset()) || isInside(region.getEndOffset() - 1);
	}

	public int getOverlappingLength(DocumentRegion region) {
		return getOverlappingLength(region.getOffset(), region.getLength());
	}

	public int getOverlappingLength(int offset, int length) {
		return Math.min(getEndOffset(), offset + length) - Math.max(getOffset(), offset);
	}

	public boolean isEmpty() {
		return length == 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + offset;
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
		DocumentRegion other = (DocumentRegion) obj;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DocumentRegion [" + offset + ", " + (offset + length) + ")";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(length);
		out.writeInt(offset);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		length = in.readInt();
		offset = in.readInt();
	}

}
