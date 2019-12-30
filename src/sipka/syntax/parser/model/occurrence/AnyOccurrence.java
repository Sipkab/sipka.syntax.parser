package sipka.syntax.parser.model.occurrence;

class AnyOccurrence extends Occurrence {
	AnyOccurrence() {
	}

	@Override
	public boolean isValidOccurrenceCount(int count) {
		return true;
	}

	@Override
	public boolean canAcceptMore(int count) {
		return count < Integer.MAX_VALUE;
	}

	@Override
	public String toString() {
		return "AnyOccurrence []";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		return Integer.MAX_VALUE - currentCount;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		return 0;
	}
}
