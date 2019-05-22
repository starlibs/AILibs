package jaicore.testproblems.enhancedttsp;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jaicore.basic.sets.SetUtil;

public class EnhancedTTSPEnumeratingSolver {
	public Collection<List<Short>> getSolutions(final EnhancedTTSP problem) {
		Set<Short> places = problem.getLocations().stream().map(Location::getId).collect(Collectors.toSet());
		places.remove((short)0);
		return SetUtil.getPermutations(places);
	}
}
