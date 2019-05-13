package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.probleminputs.builders.GraphSearchWithSubpathEvaluationsInputBuilder;

public class ForwardDecompositionHTNBestFirstPlannerFactory<IPlanning extends IHTNPlanningProblem, V extends Comparable<V>, ISearch extends GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>>
extends ForwardDecompositionHTNPlannerFactory<IPlanning, V, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>> {

	private final ForwardDecompositionReducer<IPlanning> reducer = new ForwardDecompositionReducer<>();
	private final GraphSearchWithSubpathEvaluationsInputBuilder<TFDNode, String, V> searchProblemBuilder = new GraphSearchWithSubpathEvaluationsInputBuilder<>();
	private INodeEvaluator<TFDNode, V> ne;


	public ForwardDecompositionHTNBestFirstPlannerFactory(final INodeEvaluator<TFDNode, V> ne) {
		this.ne = ne;
		super.setSearchFactory(new BestFirstFactory<>());
	}

	@Override
	public void setProblemInput(final IPlanning input) {
		super.setProblemInput(input);
		this.searchProblemBuilder.setGraphGenerator(this.reducer.encodeProblem(input).getGraphGenerator());
		this.searchProblemBuilder.setNodeEvaluator(this.ne);
		super.setSearchProblemBuilder(this.searchProblemBuilder);
	}
}
