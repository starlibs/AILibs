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
	
	private OversearchAvoidanceConfig<N, V> oversearchAvoidanceConfig;
	
	@Override
	public BestFirst<UncertainlyEvaluatedTraversalTree<N, A, V>, N, A, V> getAlgorithm() {
		if (oversearchAvoidanceConfig == null)
			throw new IllegalStateException("Uncertainty Config has not been set yet.");

		/* let the best first factory configure general aspects of the best first search */
		BestFirst<UncertainlyEvaluatedTraversalTree<N, A, V>, N, A, V> search = super.getAlgorithm();
		
		/* now set uncertainty-specific behavior */
		switch (oversearchAvoidanceConfig.getOversearchAvoidanceMode()) {
			case NONE:
				logger.warn("Usage of OversearchAvoidanceMode.NONE is deprecated! Use StandardBestFirst search instead.");
				break;
			case TWO_PHASE_SELECTION:
				if (oversearchAvoidanceConfig.getAdjustPhaseLengthsDynamically()) {
					search.setOpen(new UncertaintyExplorationOpenSelection<N, V>(
							oversearchAvoidanceConfig.getTimeout(),
							oversearchAvoidanceConfig.getInterval(),
							oversearchAvoidanceConfig.getExploitationScoreThreshold(),
							oversearchAvoidanceConfig.getExplorationUncertaintyThreshold(),
							new BasicClockModelPhaseLengthAdjuster(),
							oversearchAvoidanceConfig.getSolutionDistanceMetric(),
							new BasicExplorationCandidateSelector<N, V>(oversearchAvoidanceConfig.getMinimumSolutionDistanceForExploration())
					));
				} else {
					search.setOpen(new UncertaintyExplorationOpenSelection<N, V>(
							oversearchAvoidanceConfig.getTimeout(),
							oversearchAvoidanceConfig.getInterval(),
							oversearchAvoidanceConfig.getExploitationScoreThreshold(),
							oversearchAvoidanceConfig.getExplorationUncertaintyThreshold(),
							new IPhaseLengthAdjuster() {
								
								@Override
								public int[] getInitialPhaseLengths(int interval) {
									return new int[] {interval / 2, interval - (interval / 2)};
								}
								
								@Override
								public int[] adjustPhaseLength(int currentExplorationLength, int currentExploitationLength, long passedTime,
										long timeout) {
									return new int[] {currentExplorationLength, currentExploitationLength};
								}
							},
							oversearchAvoidanceConfig.getSolutionDistanceMetric(),
							new BasicExplorationCandidateSelector<N, V>(oversearchAvoidanceConfig.getMinimumSolutionDistanceForExploration())
					));
				}
				break;
			case PARETO_FRONT_SELECTION:
				PriorityQueue<ParetoNode<N, V>> pareto = new PriorityQueue<>(oversearchAvoidanceConfig.getParetoComperator());
				search.setOpen(new ParetoSelection<>(pareto));
				break;
			default:
				throw new UnsupportedOperationException("Mode " + oversearchAvoidanceConfig.getOversearchAvoidanceMode() + " is currently not supported.");
		}
		
		return search;
	}

	public OversearchAvoidanceConfig<N, V> getConfig() {
		return this.oversearchAvoidanceConfig;
	}

	public void setConfig(OversearchAvoidanceConfig<N, V> config) {
		this.oversearchAvoidanceConfig = config;
	}
}
