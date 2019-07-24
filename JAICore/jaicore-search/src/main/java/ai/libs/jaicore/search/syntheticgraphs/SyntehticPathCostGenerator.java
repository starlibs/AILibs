package ai.libs.jaicore.search.syntheticgraphs;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

public interface SyntehticPathCostGenerator {
	public IPathEvaluator<N, Integer, Double> create(IGraphGenerator<N, Integer> graphGenerator);
}
