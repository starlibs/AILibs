package ai.libs.jaicore.search.algorithms.standard.mcts.uuct;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class VaR implements IUCBUtilityFunction {

	private final double alpha;
	private final double b;
	private final boolean maximizeObservations = false;

	public VaR(final double alpha, final double b) {
		super();
		this.alpha = alpha;
		this.b = b;
	}

	@Override
	public double getUtility(final DoubleList observations) {
		if (observations.isEmpty()) {
			return Double.MAX_VALUE;
		}
		List<Double> inversedObs = observations.stream().map(o -> o * -1).collect(Collectors.toList());
		Collections.sort(inversedObs);
		int threshold = (int)Math.ceil(this.alpha * inversedObs.size());
		return inversedObs.get(threshold - 1);
	}

	@Override
	public double getQ() {
		return 1;
	}

	@Override
	public double getA() {
		return 1;
	}

	@Override
	public double getB() {
		return this.b;
		//		return 1 / this.alpha * (1 + 3 / Math.min(this.alpha, 1 - this.alpha)); // according to Proposition 4 in the extended version of the paper, setting c* := 1
	}
}
