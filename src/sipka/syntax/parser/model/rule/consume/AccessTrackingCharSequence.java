package sipka.syntax.parser.model.rule.consume;

class AccessTrackingCharSequence implements CharSequence {
	private final class SubCharSequence implements CharSequence {
		private final CharSequence realSub;
		private final int start;
		private final int end;

		private SubCharSequence(int start, int end) {
			this.realSub = cs.subSequence(start, end);
			this.end = end;
			this.start = start;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new SubCharSequence(this.start + start, this.start + end);
		}

		@Override
		public int length() {
			return end - start;
		}

		@Override
		public char charAt(int index) {
			int realindex = this.start + index;
			if (realindex > maxAccessIndex) {
				maxAccessIndex = realindex;
			}
			return cs.charAt(realindex);
		}

		@Override
		public String toString() {
			return realSub.toString();
		}
	}

	private CharSequence cs;
	private int maxAccessIndex = -1;

	public AccessTrackingCharSequence(CharSequence cs) {
		this.cs = cs;
	}

	public int getMaxAccessIndex() {
		return maxAccessIndex;
	}

	@Override
	public int length() {
		return cs.length();
	}

	@Override
	public char charAt(int index) {
		if (index > maxAccessIndex) {
			maxAccessIndex = index;
		}
		return cs.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new SubCharSequence(start, end);
	}

	@Override
	public String toString() {
		return cs.toString();
	}
}