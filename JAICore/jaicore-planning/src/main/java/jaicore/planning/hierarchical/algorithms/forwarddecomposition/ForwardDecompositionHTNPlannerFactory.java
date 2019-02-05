package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import jaicore.basic.algorithm.AAlgorithmFactory;
import jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.builders.SearchProblemInputBuilder;

public class ForwardDecompositionHTNPlannerFactory<IPlanner extends IHTNPlanningProblem, V extends Comparable<V>, ISearch extends GraphSearchInput<TFDNode, String>>
		extends AAlgorithmFactory<IPlanner, EvaluatedSearchGraphBasedPlan<V, TFDNode>> {

	private IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V, ?, ?> searchFactory;
	private SearchProblemInputBuilder<TFDNode, String, ISearch> searchProblemBuilder;

	public ForwardDecompositionHTNPlannerFactory() {
		super();
	}

	public ForwardDecompositionHTNPlannerFactory(IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V, ?, ?> searchFactory,
			SearchProblemInputBuilder<TFDNode, String, ISearch> searchProblemBuilder) {
		super();
		this.searchFactory = searchFactory;
		this.searchProblemBuilder = searchProblemBuilder;
	}

	public IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V, ?, ?> getSearchFactory() {
		return searchFactory;
	}

	public void setSearchFactory(IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V, ?, ?> searchFactory) {
		this.searchFactory = searchFactory;
	}

	public SearchProblemInputBuilder<TFDNode, String, ISearch> getSearchProblemBuilder() {
		return searchProblemBuilder;
	}

	public void setSearchProblemBuilder(SearchProblemInputBuilder<TFDNode, String, ISearch> searchProblemBuilder) {
		this.searchProblemBuilder = searchProblemBuilder;
	}

	@Override
	public ForwardDecompositionHTNPlanner<IPlanner, V, ISearch> getAlgorithm() {
		if (searchFactory == null)
			throw new IllegalStateException("Cannot create algorithm, search factory has not been set or set to NULL");
		if (searchProblemBuilder == null)
			throw new IllegalStateException("Cannot create algorithm, search problem builder has not been set or set to NULL");
		return new ForwardDecompositionHTNPlanner<>(getInput(), searchFactory, searchProblemBuilder);
	}
}
