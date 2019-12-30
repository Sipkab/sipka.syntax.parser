package sipka.syntax.parser.model.parse;

import sipka.syntax.parser.model.occurrence.Occurrence;

public class OccurrenceCounter {
	private Occurrence occurrence;
	private int count;

	public OccurrenceCounter() {
	}

	public OccurrenceCounter(Occurrence occurrence) {
		this.occurrence = occurrence;
	}

	public OccurrenceCounter(OccurrenceCounter counter) {
		this.occurrence = counter.occurrence;
		this.count = counter.count;
	}

	public final boolean canOccurOnceMore() {
		return occurrence.canAcceptMore(count);
	}

	public final boolean isValidOccurrenceCount() {
		return occurrence.isValidOccurrenceCount(count);
	}

	public final void addOccurrence() {
		count++;
	}

	public final void removeOccurrence() {
		if (count <= 0) {
			throw new IllegalStateException("Can't go below zero occurrences. (" + occurrence + ")");
		}
		count--;
	}

	public final int addAllOccurrenceSafely() {
		int added = occurrence.getSafelyAddCount(count);
		count = count + added;
		return added;
	}

	public final int addAnyMoreRequired() {
		int added = occurrence.getRequiredMoreCount(count);
		count = count + added;
		return added;
	}

	public void reset(Occurrence occurrence) {
		this.occurrence = occurrence;
		this.count = 0;
	}

	public void reset(OccurrenceCounter counter) {
		this.occurrence = counter.occurrence;
		this.count = counter.count;
	}

	public Occurrence getOccurrence() {
		return occurrence;
	}

	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "OccurrenceCounter [" + (occurrence != null ? "occurrence=" + occurrence + ", " : "") + "count=" + count
				+ "]";
	}

}
