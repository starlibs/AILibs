package hasco.variants.forwarddecomposition.twophase;

import hasco.core.HASCOSolutionCandidate;
import hasco.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.ml.dyadranking.search.ADyadRankedNodeQueueConfig;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;

public class TwoPhaseHASCOFactory implements SoftwareConfigurationAlgorithmFactory<TwoPhaseSoftwareConfigurationProblem, TwoPhaseHASCOReport, HASCOSolutionCandidate<Double>, Double> {

	private TwoPhaseSoftwareConfigurationProblem problem;
	private TwoPhaseHASCOConfig config;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private ADyadRankedNodeQueueConfig<TFDNode> dyadRankingConfig;

	@Override
	public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, TwoPhaseSoftwareConfigurationProblem> reducer) {
		setProblemInput(reducer.transform(problemInput));

	}

	@Override
	public void setProblemInput(TwoPhaseSoftwareConfigurationProblem problemInput) {
		this.problem = problemInput;
	}

	public TwoPhaseHASCOConfig getConfig() {
		return config;
	}

	public void setConfig(TwoPhaseHASCOConfig config) {
		this.config = config;
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}

	@Override
	public TwoPhaseHASCO getAlgorithm() {
		TwoPhaseHASCO twoPhaseHASCO = new TwoPhaseHASCO(problem, config);
		twoPhaseHASCO.setPreferredNodeEvaluator(preferredNodeEvaluator);
		if (dyadRankingConfig != null) {
			twoPhaseHASCO.setDyadRankingConfig(dyadRankingConfig);
		}
		return twoPhaseHASCO;
	}

	public void setDyadRankingConfig(ADyadRankedNodeQueueConfig<TFDNode> dyadRankingConfig) {
		this.dyadRankingConfig = dyadRankingConfig;
	}

}
