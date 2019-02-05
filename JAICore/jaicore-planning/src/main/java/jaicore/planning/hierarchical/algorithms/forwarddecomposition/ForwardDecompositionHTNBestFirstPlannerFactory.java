package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.probleminputs.builders.GraphSearchWithSubpathEvaluationsInputBuilder;

public class ForwardDecompositionHTNBestFirstPlannerFactory<PA extends Action, IPlanning extends IHTNPlanningProblem, V extends Comparable<V>, ISearch extends GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>>
		extends ForwardDecompositionHTNPlannerFactory<PA, IPlanning, V, GraphSearchWithSubpathEvaluationsInput<TFDNode, String, V>> {

	private final ForwardDecompositionReducer<PA, IPlanning> reducer = new ForwardDecompositionReducer<>();
	private final GraphSearchWithSubpathEvaluationsInputBuilder<TFDNode, String, V> searchProblemBuilder = new GraphSearchWithSubpathEvaluationsInputBuilder<>();
	private INodeEvaluator<TFDNode, V> ne;
	

	public ForwardDecompositionHTNBestFirstPlannerFactory(INodeEvaluator<TFDNode, V> ne) {
		this.ne = ne;
		super.setSearchFactory(new BestFirstFactory<>());
	}
	
	@Override
	public void setProblemInput(IPlanning input) {
		super.setProblemInput(input);
		searchProblemBuilder.setGraphGenerator(reducer.transform(input));
		searchProblemBuilder.setNodeEvaluator(ne);
		super.setSearchProblemBuilder(searchProblemBuilder);
	}
}
