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