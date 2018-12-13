package jaicore.logic.fol.algorithms.resolution;

import java.util.List;
import java.util.stream.Collectors;

public class UnitResolutionSolver extends Solver {

	@Override
	protected List<ResolutionPair> getAdmissiblePairs(List<ResolutionPair> pairs) {
		return pairs.stream().filter(p -> p.getC1().size() == 1 || p.getC2().size() == 1).collect(Collectors.toList());
	}

}
