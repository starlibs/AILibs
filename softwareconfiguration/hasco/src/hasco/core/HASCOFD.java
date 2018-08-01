package hasco.core;

import java.util.stream.Collectors;

import hasco.query.Factory;
import jaicore.basic.IObjectEvaluator;
import jaicore.planning.algorithms.IPathToPlanConverter;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlannerFactory;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.ceoctfd.CEOCTFDPathUnifier;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearchFactory;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig;
import jaicore.search.algorithms.standard.uncertainty.UncertaintyORGraphSearchFactory;

public class HASCOFD<T,V extends Comparable<V>> extends HASCO<T, TFDNode, String, V, ForwardDecompositionSolution> {

	
	public static class TFDSearchSpaceUtilFactory<V extends Comparable<V>> implements IHASCOSearchSpaceUtilFactory<TFDNode, String, V> {

		@Override
		public IPathUnification<TFDNode> getPathUnifier() {
			return new CEOCTFDPathUnifier();
		}

		@Override
		public IPathToPlanConverter<TFDNode> getPathToPlanConverter() {
			return path -> path.stream().filter(n -> n.getAppliedAction() != null).map(n -> n.getAppliedAction()).collect(Collectors.toList());
		}
	}

	public HASCOFD(IObservableORGraphSearchFactory<TFDNode, String, V> searchFactory, Factory<? extends T> converter, INodeEvaluator<TFDNode, V> nodeEvaluator,
			String nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark) {
		super(new ForwardDecompositionHTNPlannerFactory<>(), searchFactory, new TFDSearchSpaceUtilFactory<>(), nodeEvaluator, converter, nameOfRequiredInterface, benchmark);
	}

	public HASCOFD(Factory<? extends T> converter, INodeEvaluator<TFDNode, V> nodeEvaluator, String nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark) {
		this(new ORGraphSearchFactory<>(), converter, nodeEvaluator, nameOfRequiredInterface, benchmark);
	}
	
	public HASCOFD(Factory<? extends T> converter, INodeEvaluator<TFDNode, V> nodeEvaluator, String nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark, OversearchAvoidanceConfig<TFDNode, V> oversearchAvoidanceConfig) {
		this(new UncertaintyORGraphSearchFactory<>(oversearchAvoidanceConfig, new CEOCTFDPathUnifier()), converter, nodeEvaluator, nameOfRequiredInterface, benchmark);
		((UncertaintyORGraphSearchFactory<TFDNode, String, V>)getSearchFactory()).setSolutionEvaluator(getSolutionEvaluator());
	}
}
