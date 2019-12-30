package sipka.syntax.parser.model.occurrence;

class AtLeastOccurrence extends Occurrence {
	private final int min;

	public AtLeastOccurrence(int atleastCount) {
		this.min = atleastCount;
	}

	@Override
	public boolean isValidOccurrenceCount(int count) {
		return count >= min;
	}

	@Override
	public boolean canAcceptMore(int count) {
		return count < Integer.MAX_VALUE;
	}

	public int getAtleastCount() {
		return min;
	}

	@Override
	public String toString() {
		return "AtLeastOccurrence [min=" + min + "]";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		return Integer.MAX_VALUE - currentCount;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		if (currentCount < min) {
			return min - currentCount;
		}
		return 0;
	}

}
