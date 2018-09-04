package jaicore.search.testproblems.nqueens;

import java.util.Random;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IUncertaintyAnnotatingNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.algorithms.standard.uncertainty.IUncertaintySource;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.other.AgnosticPathEvaluator;
import jaicore.search.model.probleminputs.UncertainlyEvaluatedTraversalTree;

public class NQueensToUncertainlyEvaluatedTravesalTreeReducer implements AlgorithmProblemTransformer<Integer, UncertainlyEvaluatedTraversalTree<QueenNode, String, Double>> {
	
	private Random random = new Random(0);
	private int samples = 3;
	private ISolutionEvaluator<QueenNode, Double> solutionEvaluator = new AgnosticPathEvaluator<>();
	private IUncertaintySource<QueenNode, Double> uncertaintySource = (n, simulationPaths, simulationEvaluations) -> 0.5;
	
	
	@Override
	public UncertainlyEvaluatedTraversalTree<QueenNode, String, Double> transform(Integer problem) {
		GraphGenerator<QueenNode, String> graphGenerator = new NQueenGenerator(problem);
		IUncertaintyAnnotatingNodeEvaluator<QueenNode, Double> nodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(random, samples, solutionEvaluator);
		nodeEvaluator.setUncertaintySource(uncertaintySource);
		return new UncertainlyEvaluatedTraversalTree<>(graphGenerator, nodeEvaluator);

	}
}
