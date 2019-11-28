package ai.libs.jaicore.search.exampleproblems.openshop;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.PathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.problems.scheduling.openshop.OpenShopProblem;

public class OpenShopGraphSearchProblem implements IGraphSearchWithPathEvaluationsInput<OpenShopState, String, Double> {
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
	public PathGoalTester<OpenShopState, String> getGoalTester() {
		return this.gp;
	}

	@Override
	public IPathEvaluator<OpenShopState, String, Double> getPathEvaluator() {
		return p -> this.problem.getScoreOfSchedule(p.getHead().getSchedule());
	}

	public OpenShopProblem getProblem() {
		return this.problem;
	}
}
