package ai.libs.jaicore.search.exampleproblems.openshop;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.problems.scheduling.openshop.OpenShopProblem;
import ai.libs.jaicore.problems.scheduling.openshop.Operation;
import ai.libs.jaicore.problems.scheduling.openshop.Schedule;

public class OpenShopGraphSearchProblem implements IPathSearchWithPathEvaluationsInput<OpenShopState, String, Double> {
	private final OpenShopProblem problem;
	private final OpenShopGraphGenerator gg;
	private final OpenShopGoalPredicate gp = new OpenShopGoalPredicate();

	public OpenShopGraphSearchProblem(final OpenShopProblem problem) {
		super();
		this.problem = problem;
		this.gg = new OpenShopGraphGenerator(problem);
	}

	@Override
	public IGraphGenerator<OpenShopState, String> getGraphGenerator() {
		return this.gg;
	}

	@Override
	public IPathGoalTester<OpenShopState, String> getGoalTester() {
		return this.gp;
	}

	@Override
	public IPathEvaluator<OpenShopState, String, Double> getPathEvaluator() {
		return p -> {
			Schedule s = p.getHead().getSchedule();
			double baseScore = this.problem.getScoreOfSchedule(s);

			/* penalize inactive operations */
			int inActive = 0;
			for (Operation o : this.problem.getOperations().values()) {
				if (s.canOperationBeScheduledEarlierWithoutAnyOtherEffect(o)) {
					inActive ++;
				}
			}
			return baseScore + 1000 * inActive;
		};
	}

	public OpenShopProblem getProblem() {
		return this.problem;
	}
}
