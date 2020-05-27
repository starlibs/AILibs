package ai.libs.jaicore.search.algorithms.mdp.mcts.old.uuct;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public interface IUCBUtilityFunction {

	public double getUtility(DoubleList observations);

	public double getQ();

	public double getA();

	public double getB();
}
