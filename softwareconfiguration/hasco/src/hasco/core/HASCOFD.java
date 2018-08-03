package hasco.core;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import hasco.model.Component;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.query.Factory;
import jaicore.basic.IObjectEvaluator;
import jaicore.planning.algorithms.IPathToPlanConverter;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlannerFactory;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.ceoctfd.CEOCTFDPathUnifier;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.interfaces.IPathUnification;
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

	public HASCOFD(final Collection<Component> components, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig, IObservableORGraphSearchFactory<TFDNode, String, V> searchFactory, Factory<? extends T> converter,
			String nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark) {
		super(components, paramRefinementConfig, new ForwardDecompositionHTNPlannerFactory<>(), searchFactory, new TFDSearchSpaceUtilFactory<>(), converter, nameOfRequiredInterface, benchmark);
	}

	public HASCOFD(final Collection<Component> components, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig, Factory<? extends T> converter, String nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark) {
		this(components, paramRefinementConfig, new ORGraphSearchFactory<>(), converter, nameOfRequiredInterface, benchmark);
	}
	
	public HASCOFD(final Collection<Component> components, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig, Factory<? extends T> converter, String nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark, OversearchAvoidanceConfig<TFDNode> oversearchAvoidanceConfig) {
		this(components, paramRefinementConfig, new UncertaintyORGraphSearchFactory<>(oversearchAvoidanceConfig, new CEOCTFDPathUnifier()), converter, nameOfRequiredInterface, benchmark);
		((UncertaintyORGraphSearchFactory<TFDNode,String,V>)getSearchFactory()).setSolutionEvaluator(getSolutionEvaluator());
	}
}
