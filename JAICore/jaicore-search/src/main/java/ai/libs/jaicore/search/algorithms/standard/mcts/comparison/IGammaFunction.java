package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.function.Function;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;

public interface IGammaFunction extends Function<BTModel, Double> {

}
