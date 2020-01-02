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

import java.io.Serializable;

public class DocumentPosition implements Cloneable, Comparable<DocumentPosition>, Serializable {
	private static final long serialVersionUID = 5540721370308731206L;
	/* default */int positionFromStart;
	/* default */int line;
	/* default */int positionInLine;

	/* default */ DocumentPosition(int positionFromStart, int line, int positionInLine) {
		this.positionFromStart = positionFromStart;
		this.line = line;
		this.positionInLine = positionInLine;
	}

	/* default */ DocumentPosition(DocumentPosition other) {
		this.positionFromStart = other.positionFromStart;
		this.line = other.line;
		this.positionInLine = other.positionInLine;
	}

	public final int getPositionFromStart() {
		return positionFromStart;
	}

	public final int getLine() {
		return line;
	}

	public final int getPositionInLine() {
		return positionInLine;
	}

	@Override
	public String toString() {
		return "DocumentPosition [positionFromStart=" + positionFromStart + ", line=" + line + ", positionInLine="
				+ positionInLine + "]";
	}

	public String toUserString() {
		return (line + 1) + ":" + (positionInLine + 1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + line;
		result = prime * result + positionFromStart;
		result = prime * result + positionInLine;
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
		DocumentPosition other = (DocumentPosition) obj;
		if (line != other.line)
			return false;
		if (positionFromStart != other.positionFromStart)
			return false;
		if (positionInLine != other.positionInLine)
			return false;
		return true;
	}

	@Override
	public int compareTo(DocumentPosition o) {
		return Integer.compare(positionFromStart, o.positionFromStart);
	}

}
