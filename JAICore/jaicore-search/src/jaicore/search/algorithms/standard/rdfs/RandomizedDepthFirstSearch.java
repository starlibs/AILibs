package jaicore.search.algorithms.standard.rdfs;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomizedDepthFirstNodeEvaluator;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.probleminputs.GraphSearchInput;

public class RandomizedDepthFirstSearch<T, A> extends StandardBestFirst<T, A, Double> {

	private static Logger logger = LoggerFactory.getLogger(RandomizedDepthFirstSearch.class);

	public RandomizedDepthFirstSearch(GraphSearchInput<T, A> problem, Random random) {
		super(new GeneralEvaluatedTraversalTree<>(problem.getGraphGenerator(), new RandomizedDepthFirstNodeEvaluator<>(random)));
	}
}
