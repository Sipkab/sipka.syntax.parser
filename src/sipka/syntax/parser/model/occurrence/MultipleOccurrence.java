package sipka.syntax.parser.model.occurrence;

import java.util.ArrayList;
import java.util.List;

class MultipleOccurrence extends Occurrence {

	private List<Occurrence> occurs = new ArrayList<>();

	@Override
	public boolean isValidOccurrenceCount(int count) {
		for (Occurrence rule : occurs) {
			if (rule.isValidOccurrenceCount(count))
				return true;
		}
		return false;
	}

	@Override
	public boolean canAcceptMore(int count) {
		for (Occurrence rule : occurs) {
			if (rule.canAcceptMore(count))
				return true;
		}
		return false;
	}

	public void add(Occurrence rule) {
		occurs.add(rule);
	}

	@Override
	public String toString() {
		return "MultipleOccurence [occurs=" + occurs + "]";
	}

	@Override
	public int getSafelyAddCount(int currentCount) {
		int max = 0;
		for (Occurrence rule : occurs) {
			int c = rule.getSafelyAddCount(currentCount);
			if (c > max)
				max = c;
		}
		return max;
	}

	@Override
	public int getRequiredMoreCount(int currentCount) {
		int min = -1;
		for (Occurrence rule : occurs) {
			int req = rule.getRequiredMoreCount(currentCount);
			if (min < 0) {
				min = req;
			} else {
				min = Math.min(min, req);
			}
		}
		return min < 0 ? 0 : min;
	}
}
