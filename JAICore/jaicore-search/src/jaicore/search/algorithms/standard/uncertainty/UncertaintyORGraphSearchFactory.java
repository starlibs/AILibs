package jaicore.search.algorithms.standard.uncertainty;

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.BasicClockModelPhaseLengthAdjuster;
import jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.BasicExplorationCandidateSelector;
import jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.IPhaseLengthAdjuster;
import jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.UncertaintyExplorationOpenSelection;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoNode;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoSelection;
import jaicore.search.model.probleminputs.UncertainlyEvaluatedTraversalTree;

public class UncertaintyORGraphSearchFactory<N, A, V extends Comparable<V>> extends BestFirstFactory<UncertainlyEvaluatedTraversalTree<N, A, V>, N, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(UncertaintyORGraphSearchFactory.class);

	OversearchAvoidanceConfig<N, V> config;

	public OversearchAvoidanceConfig<N, V> getConfig() {
		return config;
	}

	public void setConfig(OversearchAvoidanceConfig<N, V> config) {
		this.config = config;
	}

	@Override
	public BestFirst<UncertainlyEvaluatedTraversalTree<N, A, V>, N, A, V> getAlgorithm() {
		if (config == null)
			throw new IllegalStateException("Uncertainty Config has not been set yet.");

		/* let the best first factory configure general aspects of the best first search */
		BestFirst<UncertainlyEvaluatedTraversalTree<N, A, V>, N, A, V> search = super.getAlgorithm();
		
		/* now set uncertainty-specific behavior */
		switch (config.getOversearchAvoidanceMode()) {
		case NONE: {
			logger.warn("Usage of OversearchAvoidanceMode.NONE is deprecated! Use StandardBestFirst search instead.");
			break;
		}
		case TWO_PHASE_SELECTION: {
//			search.setOpen(new UncertaintyExplorationOpenSelection<N,V>(10000, 1000, 0.1, 0.2, new BasicClockModelPhaseLengthAdjuster(), (l1,l2) -> 0.0, new BasicExplorationCandidateSelector<>(0.2)));
//			if (config.getAdjustPhaseLengthsDynamically()) {
//				search.setOpen(new UncertaintyExplorationOpenSelection<N, V>(config.getTimeout(), config.getInterval(), config.getExploitationScoreThreshold(),
//						config.getExplorationUncertaintyThreshold(), new BasicClockModelPhaseLengthAdjuster(), config.getSolutionDistanceMetric(),
//						new BasicExplorationCandidateSelector<N, V>(config.getMinimumSolutionDistanceForExploration())));
//			} else {
//				search.setOpen(new UncertaintyExplorationOpenSelection<N, V>(config.getTimeout(), config.getInterval(), config.getExploitationScoreThreshold(),
//						config.getExplorationUncertaintyThreshold(), new IPhaseLengthAdjuster() {
//
//							@Override
//							public int[] getInitialPhaseLengths(int interval) {
//								return new int[] { interval / 2, interval / 2 };
//							}
//
//							@Override
//							public int[] adjustPhaseLength(int currentExplorationLength, int currentExploitationLength, long passedTime, long timeout) {
//								return new int[] { currentExplorationLength, currentExploitationLength };
//							}
//						}, config.getSolutionDistanceMetric(), new BasicExplorationCandidateSelector<N, V>(5.0d)));
//			}
			break;
		}
		case PARETO_FRONT_SELECTION: {
			PriorityQueue<ParetoNode<N, V>> pareto = new PriorityQueue<>(config.getParetoComperator());
			search.setOpen(new ParetoSelection<>(pareto));
			break;
		}
		default:
			throw new UnsupportedOperationException("Mode " + config.getOversearchAvoidanceMode() + " is currently not supported.");
		}
		return search;
	}
}
