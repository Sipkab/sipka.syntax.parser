package sipka.syntax.parser.model.occurrence;

class AtMostOccurrence extends Occurrence {
	private final int max;

	public AtMostOccurrence(int max) {
		this.max = max;
	}

	@Override
	public boolean isValidOccurrenceCount(int count) {
		return count <= max;
	}

	@Override
	public boolean canAcceptMore(int count) {
		return count < max;
	}

	public int getMax() {
		return max;
	}

	@Override
	public String toString() {
		return "AtMaxOccurrence [max=" + max + "]";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		if (max > currentCount)
			return max - currentCount;
		return 0;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		return 0;
	}

}
