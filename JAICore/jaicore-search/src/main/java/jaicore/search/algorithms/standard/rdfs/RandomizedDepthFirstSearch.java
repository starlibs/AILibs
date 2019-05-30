package jaicore.search.algorithms.standard.rdfs;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomizedDepthFirstNodeEvaluator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.probleminputs.GraphSearchInput;

public class RandomizedDepthFirstSearch<T, A> extends StandardBestFirst<T, A, Double> {

	private Logger logger = LoggerFactory.getLogger(RandomizedDepthFirstSearch.class);
	private String loggerName;

	public RandomizedDepthFirstSearch(final GraphSearchInput<T, A> problem, final Random random) {
		super(new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), new RandomizedDepthFirstNodeEvaluator<>(random)));
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		super.setLoggerName(this.loggerName + "._bestfirst");
	}
}
