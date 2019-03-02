package jaicore.search.testproblems.enhancedttsp;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jaicore.basic.sets.SetUtil;

public class EnhancedTTSPEnumeratingSolver {
	public Collection<List<Short>> getSolutions(EnhancedTTSP problem) {
		Set<Short> places = problem.getMinTravelTimesGraph().getItems();
		places.remove((short)0);
		return SetUtil.getPermutations(places).stream().map(l -> {
			l.add(0,(short)0); // reinsert initial position
			return l;
		}).collect(Collectors.toList());
	}
}
