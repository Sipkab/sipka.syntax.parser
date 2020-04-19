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

public abstract class Occurrence {
	public static final Occurrence ANY = new AnyOccurrence();
	public static final Occurrence ZERO = new ExactOccurrence(0);
	public static final Occurrence ONCE = new ExactOccurrence(1);
	public static final Occurrence MIN_ONCE = new AtLeastOccurrence(1);
	public static final Occurrence MAX_ONCE = new AtMostOccurrence(1);
	public static final Occurrence OPTIONAL = MAX_ONCE;

	private static Occurrence parseSingle(String input) throws IllegalArgumentException {
		switch (input) {
			case "?": {
				return MAX_ONCE;
			}
			case "*": {
				return ANY;
			}
			case "+": {
				return MIN_ONCE;
			}
			default: {
				final int v;
				String[] split = null;
				if (input.endsWith("-")) {
					v = -1;
					input = input.substring(0, input.length() - 1);
				} else if (input.endsWith("+")) {
					v = 1;
					input = input.substring(0, input.length() - 1);
				} else {
					split = input.split("-");
					if (split.length == 2) {
						v = 2;
					} else {
						v = 0;
					}
				}
				try {
					switch (v) {
						case -1: {
							final int num = Integer.parseInt(input);
							if (num == 1)
								return MAX_ONCE;
							return new AtMostOccurrence(num);
						}
						case 1: {
							final int num = Integer.parseInt(input);
							switch (num) {
								case 0:
									return ANY;
								case 1:
									return MIN_ONCE;
								default:
									return new AtLeastOccurrence(num);
							}
						}
						case 0: {
							final int num = Integer.parseInt(input);
							switch (num) {
								case 0:
									return ZERO;
								case 1:
									return ONCE;
								default:
									return new ExactOccurrence(num);
							}
						}
						case 2: {
							final int num1 = Integer.parseInt(split[0]);
							final int num2 = Integer.parseInt(split[1]);
							return new RangeOccurrence(num1, num2);
						}
						default: {
							throw new IllegalArgumentException("Failed to parse occurrence");
						}
					}
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Failed to parse number: " + e.getMessage());
				}
			}
		}
	}

	public static Occurrence parse(String value) throws IllegalArgumentException {
		String[] split = value.split("[\\| \\t\\r\\n\\v\\f]+");
		Occurrence result;
		if (split.length > 1) {
			MultipleOccurrence multi = new MultipleOccurrence();
			for (String o : split) {
				multi.add(parseSingle(o));
			}
			result = multi;
		} else {
			result = parseSingle(split[0]);
		}
		return result;
	}

	public abstract boolean isValidOccurrenceCount(int count);

	public abstract boolean canAcceptMore(int count);

	public abstract int getSafelyAddCount(int currentCount);

	public abstract int getRequiredMoreCount(int currentCount);

}
