package hasco.variants.forwarddecomposition.twophase;

import hasco.core.HASCO;
import hasco.core.HASCOFactory;
import hasco.core.HASCOSolutionCandidate;
import hasco.optimizingfactory.SoftwareConfigurationAlgorithmFactory;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.probleminputs.GraphSearchInput;

public class TwoPhaseHASCOFactory<ISearch extends GraphSearchInput<N, A>, N, A> implements SoftwareConfigurationAlgorithmFactory<TwoPhaseSoftwareConfigurationProblem, HASCOSolutionCandidate<Double>, Double> {

	private HASCOFactory<ISearch, N, A, Double> hascoFactory;
	private TwoPhaseSoftwareConfigurationProblem problem;
	private TwoPhaseHASCOConfig config;
	
	public TwoPhaseHASCOFactory() {
		super();
	}

	public TwoPhaseHASCOFactory(HASCOFactory<ISearch, N, A, Double> hascoFactory) {
		super();
		this.hascoFactory = hascoFactory;
	}

	public HASCOFactory<ISearch, N, A, Double> getHascoFactory() {
		return hascoFactory;
	}

	public void setHascoFactory(HASCOFactory<ISearch, N, A, Double> hascoFactory) {
		this.hascoFactory = hascoFactory;
	}

	@Override
	public <P> void setProblemInput(P problemInput, AlgorithmicProblemReduction<P, TwoPhaseSoftwareConfigurationProblem> reducer) {
		setProblemInput(reducer.encodeProblem(problemInput));
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

	@Override
	public TwoPhaseHASCO<ISearch, N, A> getAlgorithm() {
		hascoFactory.setProblemInput(problem);
		HASCO<ISearch, N, A, Double> hasco = hascoFactory.getAlgorithm();
		TwoPhaseHASCO<ISearch, N, A> twoPhaseHASCO = new TwoPhaseHASCO<>(problem, config, hasco);
		return twoPhaseHASCO;
	}

}
