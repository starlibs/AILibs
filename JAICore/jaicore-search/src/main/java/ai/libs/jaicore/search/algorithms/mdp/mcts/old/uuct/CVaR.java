package ai.libs.jaicore.search.algorithms.mdp.mcts.old.uuct;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class CVaR implements IUCBUtilityFunction {

	private final double alpha;
	private final boolean maximize = true; // true if we want to minimize the observations

	public CVaR(final double alpha) {
		super();
		this.alpha = alpha;
	}

	@Override
	public double getUtility(final DoubleList observations) {
		List<Double> inverseList = observations.stream().map(o -> o * -1).collect(Collectors.toList());
		Collections.sort(inverseList);
		int threshold = (int)Math.ceil(this.alpha * inverseList.size());
		double sum = 0;
		if (this.maximize) {
			for (int i = 0; i < threshold; i++) {
				sum += inverseList.get(i);
			}
		}
		else {
			for (int i = threshold; i < observations.size(); i++) {
				sum += inverseList.get(i);
			}
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
