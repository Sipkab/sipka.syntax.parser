package sipka.syntax.parser.model.occurrence;

class ExactOccurrence extends Occurrence {
	private final int count;

	public ExactOccurrence(int count) {
		this.count = count;
	}

	@Override
	public boolean isValidOccurrenceCount(int count) {
		return this.count == count;
	}

	@Override
	public boolean canAcceptMore(int count) {
		return count < this.count;
	}

	@Override
	public String toString() {
		return "ExactOccurrence [count=" + count + "]";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		if (currentCount < this.count)
			return this.count - currentCount;
		return 0;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		if (currentCount < count) {
			return count - currentCount;
		}
		return 0;
	}

}
