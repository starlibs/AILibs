package ai.libs.jaicore.search.algorithms.standard.mcts.uuct;

import java.util.Collections;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class CVaR implements IUCBUtilityFunction {

	private final double alpha;

	public CVaR(final double alpha) {
		super();
		this.alpha = alpha;
	}

	@Override
	public double getUtility(final DoubleList observations) {
		Collections.sort(observations);
		int threshold = (int)Math.ceil(this.alpha * observations.size());
		double sum = 0;
		for (int i = 0; i < threshold; i++) {
			sum += observations.getDouble(i);
		}
		return sum / threshold;
	}

	@Override
	public double getQ() {
		return 2;
	}

	@Override
	public double getA() {
		return 1;
	}

	@Override
	public double getB() {
		return 1 / this.alpha * (1 + 3 / Math.min(this.alpha, 1 - this.alpha)); // according to Proposition 4 in the extended version of the paper, setting c* := 1
	}
}
