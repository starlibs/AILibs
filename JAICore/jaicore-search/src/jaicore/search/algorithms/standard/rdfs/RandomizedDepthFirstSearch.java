package jaicore.search.algorithms.standard.rdfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomizedDepthFirstNodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class RandomizedDepthFirstSearch<T, A> extends StandardBestFirst<T, A, Double> {

	private static Logger logger = LoggerFactory.getLogger(RandomizedDepthFirstSearch.class);

	public RandomizedDepthFirstSearch(GeneralEvaluatedTraversalTree<T, A, Double> problem, Random random) {
		super(new GeneralEvaluatedTraversalTree<>(new GraphGenerator<T, A>() {

			@Override
			public RootGenerator<T> getRootGenerator() {
				return problem.getGraphGenerator().getRootGenerator();
			}

			@Override
			public SuccessorGenerator<T, A> getSuccessorGenerator() {
				if (!(problem.getGraphGenerator().getSuccessorGenerator() instanceof SingleSuccessorGenerator)) {
					logger.warn(
							"The successor generator of the given graph generator does not implement SingleSuccessorGenerator. This may significantly slow down the randomized depth first search.");
					return problem.getGraphGenerator().getSuccessorGenerator();
				} else {
					SingleSuccessorGenerator<T, A> successorGenerator = (SingleSuccessorGenerator<T, A>) problem.getGraphGenerator()
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
				return problem.getGraphGenerator().getGoalTester();
			}

			@Override
			public boolean isSelfContained() {
				return problem.getGraphGenerator().isSelfContained();
			}

			@Override
			public void setNodeNumbering(boolean nodenumbering) {
				throw new UnsupportedOperationException("Not implemented");
			}
		}, new RandomizedDepthFirstNodeEvaluator<>(random)));
	}
}
