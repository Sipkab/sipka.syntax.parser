package sipka.syntax.parser.model.occurrence;

class RangeOccurrence extends Occurrence {

	private final int rangeStart;
	private final int rangeEnd;

	public RangeOccurrence(int rangeStart, int rangeEnd) {
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
	}

	@Override
	public boolean isValidOccurrenceCount(int count) {
		return count >= rangeStart && count <= rangeEnd;
	}

	@Override
	public boolean canAcceptMore(int count) {
		return count < rangeEnd;
	}

	@Override
	public String toString() {
		return "RangeOccurrence [rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd + "]";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		if (currentCount < rangeEnd)
			return rangeEnd - currentCount;
		return 0;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		if (currentCount < rangeStart) {
			return rangeEnd - currentCount;
		}
		return 0;
	}
}
