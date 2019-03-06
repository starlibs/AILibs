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

	private IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V> searchFactory;
	private SearchProblemInputBuilder<TFDNode, String, ISearch> searchProblemBuilder;

	public ForwardDecompositionHTNPlannerFactory() {
		super();
	}

	public ForwardDecompositionHTNPlannerFactory(final IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V> searchFactory,
			final SearchProblemInputBuilder<TFDNode, String, ISearch> searchProblemBuilder) {
		super();
		this.searchFactory = searchFactory;
		this.searchProblemBuilder = searchProblemBuilder;
	}

	public IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V> getSearchFactory() {
		return this.searchFactory;
	}

	public void setSearchFactory(final IOptimalPathInORGraphSearchFactory<ISearch, TFDNode, String, V> searchFactory) {
		this.searchFactory = searchFactory;
	}

	public SearchProblemInputBuilder<TFDNode, String, ISearch> getSearchProblemBuilder() {
		return this.searchProblemBuilder;
	}

	public void setSearchProblemBuilder(final SearchProblemInputBuilder<TFDNode, String, ISearch> searchProblemBuilder) {
		this.searchProblemBuilder = searchProblemBuilder;
	}

	@Override
	public ForwardDecompositionHTNPlanner<IPlanner, V, ISearch> getAlgorithm() {
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public ForwardDecompositionHTNPlanner<IPlanner, V, ISearch> getAlgorithm(final IPlanner input) {
		if (this.searchFactory == null) {
			throw new IllegalStateException("Cannot create algorithm, search factory has not been set or set to NULL");
		}
		if (this.searchProblemBuilder == null) {
			throw new IllegalStateException("Cannot create algorithm, search problem builder has not been set or set to NULL");
		}
		return new ForwardDecompositionHTNPlanner<>(this.getInput(), this.searchFactory, this.searchProblemBuilder);
	}
}
