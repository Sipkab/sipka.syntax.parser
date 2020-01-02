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
package sipka.syntax.parser.model.statement.repair;

import java.util.Objects;

public class ReparationRegion {
	private int offset;
	private int length;
	private CharSequence text;

	public ReparationRegion(int offset, int length, CharSequence text) {
		this.offset = offset;
		this.length = length;
		this.text = text;
	}

	@Override
	public String toString() {
		return "ReparationRegion [" + offset + " (" + length + "): " + (text == null ? 0 : text.length()) + "]";
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public CharSequence getText() {
		return text;
	}

	public void apply(StringBuilder chars, int charsoffset) {
		chars.replace(offset - charsoffset, offset + length - charsoffset, Objects.toString(getText(), ""));
	}

	public void apply(StringBuilder chars) {
		chars.replace(offset, offset + length, Objects.toString(getText(), ""));
	}

	public String apply(CharSequence chars, int charsoffset) {
		StringBuilder sb = new StringBuilder();
		int firstlen = getOffset() - charsoffset;
		sb.append(chars.subSequence(0, firstlen));
		CharSequence text = getText();
		if (text != null) {
			sb.append(text);
		}
		sb.append(chars.subSequence(firstlen + getLength(), chars.length()));
		return sb.toString();
	}

}
