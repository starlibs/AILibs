package jaicore.search.algorithms.standard.uncertainty;

import java.util.PriorityQueue;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.BasicClockModelPhaseLengthAdjuster;
import jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.BasicExplorationCandidateSelector;
import jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.IPhaseLengthAdjuster;
import jaicore.search.algorithms.standard.uncertainty.explorationexploitationsearch.UncertaintyExplorationOpenSelection;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoNode;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoSelection;
import jaicore.search.structure.core.GraphGenerator;

public class UncertaintyORGraphSearchFactory <T, A> implements IObservableORGraphSearchFactory<T, A, Double>{

	private static final Logger logger = LoggerFactory.getLogger(UncertaintyORGraphSearchFactory.class);

	private OversearchAvoidanceConfig<T> oversearchAvoidanceConfig;
	private IPathUnification<T> pathUnification;
	private ISolutionEvaluator<T, Double> solutionEvaluator;
	private int timeoutForFInMS;
	private INodeEvaluator<T, Double> timeoutEvaluator;
	private String loggerName;
	
	public UncertaintyORGraphSearchFactory(OversearchAvoidanceConfig<T> oversearchAvoidanceConfig, IPathUnification<T> pathUnification) {
		this.oversearchAvoidanceConfig = oversearchAvoidanceConfig;
		this.pathUnification = pathUnification;
	}
	
	@Override
	public IObservableORGraphSearch<T, A, Double> createSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Double> nodeEvaluator, int numberOfCPUs) {
		ORGraphSearch<T, A, Double> search;
		if (oversearchAvoidanceConfig.getOversearchAvoidanceMode() == OversearchAvoidanceConfig.OversearchAvoidanceMode.NONE) {
			search = new ORGraphSearch<>(graphGenerator, nodeEvaluator);
		} else {
			search = new ORGraphSearch<>(
					graphGenerator,
					new UncertaintyRandomCompletionEvaluator<T, A, Double>(
						new Random(oversearchAvoidanceConfig.getSeed()),
						oversearchAvoidanceConfig.getRandomSampleAmount(),
						pathUnification,
						this.solutionEvaluator,
						new BasicUncertaintySource<T>()
					)
			);
			
			if (oversearchAvoidanceConfig.getOversearchAvoidanceMode() == OversearchAvoidanceConfig.OversearchAvoidanceMode.TWO_PHASE_SELECTION) {
				if (oversearchAvoidanceConfig.getAdjustPhaseLengthsDynamically()) {
					search.setOpen(new UncertaintyExplorationOpenSelection<T, Double>(
							oversearchAvoidanceConfig.getTimeout(),
							oversearchAvoidanceConfig.getInterval(),
							oversearchAvoidanceConfig.getExploitationScoreThreshold(),
							oversearchAvoidanceConfig.getExplorationUncertaintyThreshold(),
							new BasicClockModelPhaseLengthAdjuster(),
							oversearchAvoidanceConfig.getSolutionDistanceMetric(),
							new BasicExplorationCandidateSelector<T, Double>(oversearchAvoidanceConfig.getMinimumSolutionDistanceForExploration())
					));
				} else {
					search.setOpen(new UncertaintyExplorationOpenSelection<T, Double>(
							oversearchAvoidanceConfig.getTimeout(),
							oversearchAvoidanceConfig.getInterval(),
							oversearchAvoidanceConfig.getExploitationScoreThreshold(),
							oversearchAvoidanceConfig.getExplorationUncertaintyThreshold(),
							new IPhaseLengthAdjuster() {
								
								@Override
								public int[] getInitialPhaseLengths(int interval) {
									return new int[] {interval / 2, interval / 2};
								}
								
								@Override
								public int[] adjustPhaseLength(int currentExplorationLength, int currentExploitationLength, long passedTime,
										long timeout) {
									return new int[] {currentExplorationLength, currentExploitationLength};
								}
							},
							oversearchAvoidanceConfig.getSolutionDistanceMetric(),
							new BasicExplorationCandidateSelector<T, Double>(5.0d)
					));
				}
			} else {
				PriorityQueue<ParetoNode<T, Double>> pareto = new PriorityQueue<>(oversearchAvoidanceConfig.getParetoComperator());
				search.setOpen(new ParetoSelection<>(pareto));
			}
		}
		
		search.parallelizeNodeExpansion(numberOfCPUs);
		search.setTimeoutForComputationOfF(this.timeoutForFInMS, this.timeoutEvaluator);
		if (loggerName != null && loggerName.length() > 0)
			search.setLoggerName(loggerName);
		return search;
	}
	
	public void setTimeoutForFComputation(final int timeoutInMS, final INodeEvaluator<T, Double> timeoutEvaluator) {
		this.timeoutForFInMS = timeoutInMS;
		this.timeoutEvaluator = timeoutEvaluator;
	}

	public int getTimeoutForFInMS() {
		return this.timeoutForFInMS;
	}

	public INodeEvaluator<T, Double> getTimeoutEvaluator() {
		return this.timeoutEvaluator;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public UncertaintyORGraphSearchFactory<T, A> setSolutionEvaluator(ISolutionEvaluator<T, Double> solutionEvaluator) {
		this.solutionEvaluator = solutionEvaluator;
		return this;
	}
	
}
