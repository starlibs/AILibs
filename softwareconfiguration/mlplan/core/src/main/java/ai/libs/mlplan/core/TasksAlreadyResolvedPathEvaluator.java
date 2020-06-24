package ai.libs.mlplan.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

public class TasksAlreadyResolvedPathEvaluator implements IPathEvaluator<TFDNode, String, Double> {

	private Set<String> prefixesWhichHaveToBeResolvedBeforeGoingToNextPhase = new HashSet<>();

	public TasksAlreadyResolvedPathEvaluator(final List<String> prefixesWhichHaveToBeResolvedBeforeGoingToNextPhase) {
		this.prefixesWhichHaveToBeResolvedBeforeGoingToNextPhase = new HashSet<>(prefixesWhichHaveToBeResolvedBeforeGoingToNextPhase);
	}

	@Override
	public Double evaluate(final ILabeledPath<TFDNode, String> path) throws PathEvaluationException, InterruptedException {
		Set<String> openTasks = path.getHead().getRemainingTasks().stream().map(Literal::getPropertyName).collect(Collectors.toSet());
		for (String prefix : this.prefixesWhichHaveToBeResolvedBeforeGoingToNextPhase) {
			if (openTasks.stream().anyMatch(t -> t.startsWith("1_tResolve" + prefix))) {
				return 0d;
			}
		}
		return null;
	}

}
