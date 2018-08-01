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

public class UncertaintyORGraphSearchFactory <T, A, V extends Comparable<V>> implements IObservableORGraphSearchFactory<T, A, V>{

	private static final Logger logger = LoggerFactory.getLogger(UncertaintyORGraphSearchFactory.class);

	private OversearchAvoidanceConfig<T> oversearchAvoidanceConfig;
	private IPathUnification<T> pathUnification;
	private ISolutionEvaluator<T, V> solutionEvaluator;
	private int timeoutForFInMS;
	private INodeEvaluator<T, V> timeoutEvaluator;
	private String loggerName;
	private IUncertaintySource<T, V> uncertaintySource;
	
	public UncertaintyORGraphSearchFactory(OversearchAvoidanceConfig<T> oversearchAvoidanceConfig, IPathUnification<T> pathUnification) {
		this.oversearchAvoidanceConfig = oversearchAvoidanceConfig;
		this.pathUnification = pathUnification;
	}
	
	@Override
	public IObservableORGraphSearch<T, A, V> createSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> nodeEvaluator, int numberOfCPUs) {
		ORGraphSearch<T, A, V> search;
		if (oversearchAvoidanceConfig.getOversearchAvoidanceMode() == OversearchAvoidanceConfig.OversearchAvoidanceMode.NONE) {
			search = new ORGraphSearch<>(graphGenerator, nodeEvaluator);
		} else {
			if (uncertaintySource == null)
				throw new IllegalArgumentException("Cannot create search as uncertainty source has not been set. Use the respective getter.");
			search = new ORGraphSearch<T,A,V>(
					graphGenerator,
					new UncertaintyRandomCompletionEvaluator<T, A, V>(
						new Random(oversearchAvoidanceConfig.getSeed()),
						oversearchAvoidanceConfig.getRandomSampleAmount(),
						pathUnification,
						this.solutionEvaluator,
						uncertaintySource
					)
			);
			
			if (oversearchAvoidanceConfig.getOversearchAvoidanceMode() == OversearchAvoidanceConfig.OversearchAvoidanceMode.TWO_PHASE_SELECTION) {
				if (oversearchAvoidanceConfig.getAdjustPhaseLengthsDynamically()) {
					search.setOpen(new UncertaintyExplorationOpenSelection<T, V>(
							oversearchAvoidanceConfig.getTimeout(),
							oversearchAvoidanceConfig.getInterval(),
							oversearchAvoidanceConfig.getExploitationScoreThreshold(),
							oversearchAvoidanceConfig.getExplorationUncertaintyThreshold(),
							new BasicClockModelPhaseLengthAdjuster(),
							oversearchAvoidanceConfig.getSolutionDistanceMetric(),
							new BasicExplorationCandidateSelector<T, V>(oversearchAvoidanceConfig.getMinimumSolutionDistanceForExploration())
					));
				} else {
					search.setOpen(new UncertaintyExplorationOpenSelection<T, V>(
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
							new BasicExplorationCandidateSelector<T, V>(5.0d)
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
	
	public void setTimeoutForFComputation(final int timeoutInMS, final INodeEvaluator<T, V> timeoutEvaluator) {
		this.timeoutForFInMS = timeoutInMS;
		this.timeoutEvaluator = timeoutEvaluator;
	}

	public int getTimeoutForFInMS() {
		return this.timeoutForFInMS;
	}

	public INodeEvaluator<T, V> getTimeoutEvaluator() {
		return this.timeoutEvaluator;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public UncertaintyORGraphSearchFactory<T, A, V> setSolutionEvaluator(ISolutionEvaluator<T, V> solutionEvaluator) {
		this.solutionEvaluator = solutionEvaluator;
		return this;
	}

	public IUncertaintySource<T, V> getUncertaintySource() {
		return uncertaintySource;
	}

	public void setUncertaintySource(IUncertaintySource<T, V> uncertaintySource) {
		this.uncertaintySource = uncertaintySource;
	}
}
