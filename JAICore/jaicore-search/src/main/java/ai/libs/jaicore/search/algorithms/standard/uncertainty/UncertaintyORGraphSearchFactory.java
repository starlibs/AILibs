package ai.libs.jaicore.search.algorithms.standard.uncertainty;

import java.util.PriorityQueue;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyUncertaintyAnnotatingPathEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import ai.libs.jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.BasicClockModelPhaseLengthAdjuster;
import ai.libs.jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.BasicExplorationCandidateSelector;
import ai.libs.jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.IPhaseLengthAdjuster;
import ai.libs.jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.UncertaintyExplorationOpenSelection;
import ai.libs.jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoSelection;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithUncertaintyBasedSubpathEvaluationInput;

public class UncertaintyORGraphSearchFactory<N, A, V extends Comparable<V>>
extends BestFirstFactory<GraphSearchWithUncertaintyBasedSubpathEvaluationInput<N, A, V>, N, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(UncertaintyORGraphSearchFactory.class);

	private OversearchAvoidanceConfig<N, A,V> oversearchAvoidanceConfig;

	@Override
	public BestFirst<GraphSearchWithUncertaintyBasedSubpathEvaluationInput<N, A, V>, N, A, V> getAlgorithm() {
		if (this.oversearchAvoidanceConfig == null) {
			throw new IllegalStateException("Uncertainty Config has not been set yet.");
		}

		/*
		 * let the best first factory configure general aspects of the best first search
		 */
		BestFirst<GraphSearchWithUncertaintyBasedSubpathEvaluationInput<N, A, V>, N, A, V> search = super.getAlgorithm();

		/* check that node evaluator supports uncertainty */
		if (!(search.getNodeEvaluator() instanceof IPotentiallyUncertaintyAnnotatingPathEvaluator)) {
			throw new UnsupportedOperationException("Cannot create uncertainty based search with node evaluator " + search.getNodeEvaluator().getClass().getName() + ", which does not implement " + IPotentiallyUncertaintyAnnotatingPathEvaluator.class.getName());
		}
		if (!((IPotentiallyUncertaintyAnnotatingPathEvaluator<?,?,?>)search.getNodeEvaluator()).annotatesUncertainty()) {
			throw new UnsupportedOperationException("The given node evaluator supports uncertainty annotation, but it declares that it will not annotate uncertainty. Maybe no uncertainty source has been defined.");
		}

		/* now set uncertainty-specific behavior */
		switch (this.oversearchAvoidanceConfig.getOversearchAvoidanceMode()) {
		case NONE:
			logger.warn("Usage of OversearchAvoidanceMode.NONE is deprecated! Use StandardBestFirst search instead.");
			break;
		case TWO_PHASE_SELECTION:
			if (this.oversearchAvoidanceConfig.getAdjustPhaseLengthsDynamically()) {
				search.setOpen(new UncertaintyExplorationOpenSelection<N, A, V>(
						this.oversearchAvoidanceConfig.getTimeout(),
						this.oversearchAvoidanceConfig.getInterval(),
						this.oversearchAvoidanceConfig.getExploitationScoreThreshold(),
						this.oversearchAvoidanceConfig.getExplorationUncertaintyThreshold(),
						new BasicClockModelPhaseLengthAdjuster(),
						this.oversearchAvoidanceConfig.getSolutionDistanceMetric(),
						new BasicExplorationCandidateSelector<N, A, V>(this.oversearchAvoidanceConfig.getMinimumSolutionDistanceForExploration())
						));
			} else {
				search.setOpen(new UncertaintyExplorationOpenSelection<N, A,V>(
						this.oversearchAvoidanceConfig.getTimeout(),
						this.oversearchAvoidanceConfig.getInterval(),
						this.oversearchAvoidanceConfig.getExploitationScoreThreshold(),
						this.oversearchAvoidanceConfig.getExplorationUncertaintyThreshold(),
						new IPhaseLengthAdjuster() {

							@Override
							public int[] getInitialPhaseLengths(final int interval) {
								return new int[] {interval / 2, interval - (interval / 2)};
							}

							@Override
							public int[] adjustPhaseLength(final int currentExplorationLength, final int currentExploitationLength, final long passedTime,
									final long timeout) {
								return new int[] {currentExplorationLength, currentExploitationLength};
							}
						},
						this.oversearchAvoidanceConfig.getSolutionDistanceMetric(),
						new BasicExplorationCandidateSelector<N, A,V>(this.oversearchAvoidanceConfig.getMinimumSolutionDistanceForExploration())
						));
			}
			break;
		case PARETO_FRONT_SELECTION:
			PriorityQueue<BackPointerPath<N,A, V>> pareto = new PriorityQueue<>(this.oversearchAvoidanceConfig.getParetoComperator());
			search.setOpen(new ParetoSelection<>(pareto));
			break;
		default:
			throw new UnsupportedOperationException("Mode " + this.oversearchAvoidanceConfig.getOversearchAvoidanceMode() + " is currently not supported.");
		}

		return search;
	}

	public OversearchAvoidanceConfig<N, A,V> getConfig() {
		return this.oversearchAvoidanceConfig;
	}

	public void setConfig(final OversearchAvoidanceConfig<N,A, V> config) {
		this.oversearchAvoidanceConfig = config;
	}
}
