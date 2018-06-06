package jaicore.basic.sets;

import java.util.LinkedList;
import java.util.List;

public class PartialOrderedSetUtil {
	
	private final PartialOrderedSet<Integer> set;

	public PartialOrderedSetUtil(final PartialOrderedSet<Integer> set) {
		this.set = set;
	}
	
	public int calc() {
		if (set.size() == 1) {
			return 1;
		}
		if (set.size() == 0) {
			return 0;
		}
		final List<Integer> list = new LinkedList<>(this.set);
		return getNumberOfAllowedPermutations(new LinkedList<>(), list);
	}
	
	private int getNumberOfAllowedPermutations(final List<Integer> prefix, final List<Integer> suffix) {
		if (suffix.size() == 0) {
			return isCompatible(prefix) ? 1 : 0;
		}
		int numberOfCompatiblePermutations = 0;
		for (int i = 0; i < suffix.size(); i++) {
			final List<Integer> newPrefix = new LinkedList<>(prefix);
			newPrefix.add(suffix.get(i));
		}
		return numberOfCompatiblePermutations;
	}
	
	private boolean isCompatible(final List<Integer> totalOrder) {
		for (int i = 0; i < totalOrder.size() - 1; i++) {
			for (int j = i+1; j < totalOrder.size(); j++) {
				if (!set.allowsABeforeB(totalOrder.get(i), totalOrder.get(j))) {
					return false;
				}
			}
		}
		return true;
	}

}
