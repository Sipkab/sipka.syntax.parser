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

public class ValueConsumer {
	private StringBuilder valueBuilder = new StringBuilder();

	public void appendValue(CharSequence parsed) {
		valueBuilder.append(parsed);
	}

	public void appendValue(String parsed) {
		valueBuilder.append(parsed);
	}

	protected CharSequence getParsedValue() {
		return valueBuilder;
	}

	public int length() {
		return valueBuilder.length();
	}

	public void setLength(int len) {
		valueBuilder.setLength(len);
	}

	@Override
	public String toString() {
		return "ValueConsumer [builder=" + valueBuilder + "]";
	}
}