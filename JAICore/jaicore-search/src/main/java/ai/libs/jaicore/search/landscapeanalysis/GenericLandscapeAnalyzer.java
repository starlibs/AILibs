package ai.libs.jaicore.search.landscapeanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class GenericLandscapeAnalyzer<N, A> {
	private final IPathSearchWithPathEvaluationsInput<N, A, Double> problem;
	private final N root;
	private final ISuccessorGenerator<N, A> successorGenerator;
	private final IPathGoalTester<N, A> goalTester;
	private double min = Double.MAX_VALUE;

	public GenericLandscapeAnalyzer(final IPathSearchWithPathEvaluationsInput<N, A, Double> problem) {
		super();
		this.problem = problem;
		this.root = problem.getGraphGenerator().getRootGenerator().getRoots().iterator().next();
		this.successorGenerator = problem.getGraphGenerator().getSuccessorGenerator();
		this.goalTester = problem.getGoalTester();
	}

	public double[] getValues(final Number probeSize, final LandscapeAnalysisCompletionTechnique technique) throws InterruptedException, PathEvaluationException {
		return this.getValues(new SearchGraphPath<>(this.root), probeSize, technique);
	}

	public double[] getValues(final List<Integer> decisions, final int probeSize, final LandscapeAnalysisCompletionTechnique technique) throws InterruptedException, PathEvaluationException {
		List<N> nodes = new ArrayList<>(decisions.size() + 1);
		List<A> arcs = new ArrayList<>(decisions.size());
		N current = this.root;
		nodes.add(current);
		for (int child : decisions) {
			INewNodeDescription<N, A> ned = this.successorGenerator.generateSuccessors(current).get(child);
			current = ned.getTo();
			nodes.add(current);
			arcs.add(ned.getArcLabel());
		}
		ILabeledPath<N,A> path = new SearchGraphPath<>(nodes, arcs);
		return this.getValues(path, probeSize, technique);
	}

	public double[] getValues(final ILabeledPath<N, A> path, final Number probeSize, final LandscapeAnalysisCompletionTechnique technique) throws InterruptedException, PathEvaluationException {
		List<Double> values = this.probeUnderPath(path, probeSize, technique);
		int n = values.size();
		double[] valuesAsArray = new double[n];
		for (int i = 0; i < n; i++) {
			valuesAsArray[i] = values.get(i);
		}
		return valuesAsArray;
	}

	private List<Double> probeUnderPath(final ILabeledPath<N, A> path, final Number limit, final LandscapeAnalysisCompletionTechnique technique) throws InterruptedException, PathEvaluationException {
		N node = path.getHead();
		int cLimit = limit.intValue();
		List<Double> scoresUnderChildren = new ArrayList<>(cLimit);
		if (this.goalTester.isGoal(path)) {
			double score = this.problem.getPathEvaluator().evaluate(path);
			if (score < this.min) {
				this.min = score;
			}
			scoresUnderChildren.add(score);
			return scoresUnderChildren;
		}
		List<INewNodeDescription<N, A>> successors = this.successorGenerator.generateSuccessors(node);
		int n = successors.size();

		/* if we cannot delve into all successors, order them by the defined technique */
		if (n > cLimit) {
			switch (technique) {
			case FIRST:

				/* do nothing */
				break;
			case LAST:
				Collections.reverse(successors);
				break;
			case RANDOM:
				Collections.shuffle(successors);
				break;
			}
		}

		int limitPerChild = (int)Math.floor(cLimit * 1.0 / n);
		int numberOfChildrenWithExtra = cLimit % n;
		for (int child = 0; child < n; child++) {
			int limitForThisChild = limitPerChild + (child < numberOfChildrenWithExtra ? 1 : 0);
			if (limitForThisChild <= 0) {
				return scoresUnderChildren;
			}
			ILabeledPath<N, A> newPath = new SearchGraphPath<>(path, successors.get(child).getTo(), successors.get(child).getArcLabel());
			scoresUnderChildren.addAll(this.probeUnderPath(newPath, limitForThisChild, technique));
		}
		return scoresUnderChildren;
	}

	public List<List<double[]>> getIterativeProbeValuesAlongRandomPath(final Number probSizePerLevelAndChild) throws PathEvaluationException, InterruptedException {
		ILabeledPath<N, A> currentPath = new SearchGraphPath<>(this.root);
		while (!this.goalTester.isGoal(currentPath)) {
			List<INewNodeDescription<N, A>> nedList = this.problem.getGraphGenerator().getSuccessorGenerator().generateSuccessors(currentPath.getHead());
			Collections.shuffle(nedList);
			currentPath = new SearchGraphPath<>(currentPath, nedList.get(0).getTo(), nedList.get(0).getArcLabel());
		}
		System.out.println("Drew path " + currentPath.getArcs() + ": " + currentPath.getHead());
		return this.getIterativeProbeValues(currentPath, probSizePerLevelAndChild);
	}

	public List<List<double[]>> getIterativeProbeValues(final ILabeledPath<N, A> path, final Number probSizePerLevelAndChild) throws PathEvaluationException, InterruptedException {
		List<List<double[]>> iterativeProbes = new ArrayList<>();
		for (int depth = 0; depth < path.getNumberOfNodes() - 1; depth++) {
			System.out.println("Probing on level " + depth);

			/* compute sub-path of the relevant depth */
			ILabeledPath<N, A> subPath = path;
			while (subPath.getNumberOfNodes() > depth + 1) {
				subPath = subPath.getPathToParentOfHead();
			}

			/* compute successors in that depth */
			List<INewNodeDescription<N, A>> nedList = this.problem.getGraphGenerator().getSuccessorGenerator().generateSuccessors(subPath.getHead());

			/* sample under each of the nodes */
			List<double[]> probesOnLevel = new ArrayList<>(nedList.size());
			for (INewNodeDescription<N, A> ned : nedList) {
				ILabeledPath<N, A> extendedPath = new SearchGraphPath<>(subPath, ned.getTo(), ned.getArcLabel());
				double[] landscape = this.getValues(extendedPath, probSizePerLevelAndChild, LandscapeAnalysisCompletionTechnique.RANDOM);
				probesOnLevel.add(landscape);
			}
			iterativeProbes.add(probesOnLevel);
		}
		return iterativeProbes;
	}
}