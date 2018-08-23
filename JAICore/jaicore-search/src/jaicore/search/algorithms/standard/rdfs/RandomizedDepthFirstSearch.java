package jaicore.search.algorithms.standard.rdfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class RandomizedDepthFirstSearch<T, A> extends BestFirst<T, A> {

	private static Logger logger = LoggerFactory.getLogger(RandomizedDepthFirstSearch.class);

	public RandomizedDepthFirstSearch(GraphGenerator<T, A> graphGenerator, Random random) {
		super(new GraphGenerator<T, A>() {

			@Override
			public RootGenerator<T> getRootGenerator() {
				return graphGenerator.getRootGenerator();
			}

			@Override
			public SuccessorGenerator<T, A> getSuccessorGenerator() {
				if (!(graphGenerator.getSuccessorGenerator() instanceof SingleSuccessorGenerator)) {
					logger.warn(
							"The successor generator of the given graph generator does not implement SingleSuccessorGenerator. This may significantly slow down the randomized depth first search.");
					return graphGenerator.getSuccessorGenerator();
				} else {
					SingleSuccessorGenerator<T, A> successorGenerator = (SingleSuccessorGenerator<T, A>) graphGenerator
							.getSuccessorGenerator();
					return new SuccessorGenerator<T, A>() {

						@Override
						public List<NodeExpansionDescription<T, A>> generateSuccessors(T node) {
							List<NodeExpansionDescription<T, A>> successors = new ArrayList<>();
							int i = Math.abs(random.nextInt());
							successors.add(successorGenerator.generateSuccessor(node, i));
							return successors;
						}
					};
				}
			}

			@Override
			public GoalTester<T> getGoalTester() {
				return graphGenerator.getGoalTester();
			}

			@Override
			public boolean isSelfContained() {
				return graphGenerator.isSelfContained();
			}

			@Override
			public void setNodeNumbering(boolean nodenumbering) {
				throw new UnsupportedOperationException("Not implemented");
			}

		}, n -> 0.0);

	}
}
