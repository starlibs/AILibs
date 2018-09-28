package hasco.core;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import hasco.core.HASCOFD.TFDSearchSpaceUtilFactory;
import hasco.model.Component;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.query.Factory;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.SQLAdapter;
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

public class HASCOFDWithParameterPruning<T, V extends Comparable<V>>
		extends HASCOWithParameterPruning<T, TFDNode, String, V, ForwardDecompositionSolution> {

	public static class TFDSearchSpaceUtilFactory<V extends Comparable<V>>
			implements IHASCOSearchSpaceUtilFactory<TFDNode, String, V> {

		@Override
		public IPathUnification<TFDNode> getPathUnifier() {
			return new CEOCTFDPathUnifier();
		}

		@Override
		public IPathToPlanConverter<TFDNode> getPathToPlanConverter() {
			return path -> path.stream().filter(n -> n.getAppliedAction() != null).map(n -> n.getAppliedAction())
					.collect(Collectors.toList());
		}
	}

	// public HASCOFDWithParameterPruning(final Collection<Component> components,
	// Map<Component, Map<Parameter, ParameterRefinementConfiguration>>
	// paramRefinementConfig,IObservableORGraphSearchFactory<TFDNode, String,
	// Double> searchFactory,
	// Factory<? extends T> converter, INodeEvaluator<TFDNode, Double>
	// nodeEvaluator,
	// String nameOfRequiredInterface, IObjectEvaluator<T, Double> benchmark) {
	// super(components,paramRefinementConfig,new
	// ForwardDecompositionHTNPlannerFactory<>(), searchFactory, new
	// TFDSearchSpaceUtilFactory(),
	// nodeEvaluator, converter, nameOfRequiredInterface, benchmark, 0, 0, false);
	// }
	//
	// /*
	// * Constructor with arguments for jmhansel parameter importance feature
	// */
	// public HASCOFDWithParameterPruning(final Collection<Component> components,
	// Map<Component, Map<Parameter, ParameterRefinementConfiguration>>
	// paramRefinementConfig, Factory<? extends T> converter, String
	// nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark, double
	// importanceThreshold,
	// int minNumSamplesForImportanceEstimation, boolean useImportanceEstimation) {
	// super(components,paramRefinementConfig,new
	// ForwardDecompositionHTNPlannerFactory<>(), searchFactory, new
	// TFDSearchSpaceUtilFactory(),
	// nodeEvaluator, converter, nameOfRequiredInterface, benchmark,
	// importanceThreshold,
	// minNumSamplesForImportanceEstimation, useImportanceEstimation);
	// }
	//
	// public HASCOFDWithParameterPruning(final Collection<Component> components,
	// Map<Component, Map<Parameter, ParameterRefinementConfiguration>>
	// paramRefinementConfig,Factory<? extends T> converter, INodeEvaluator<TFDNode,
	// Double> nodeEvaluator,
	// String nameOfRequiredInterface, IObjectEvaluator<T, Double> benchmark, double
	// importanceThreshold,
	// int minNumSamplesForImportanceEstimation, boolean useImportanceEstimation) {
	// this(components,paramRefinementConfig,new ORGraphSearchFactory<>(),
	// converter, nodeEvaluator, nameOfRequiredInterface, benchmark,
	// importanceThreshold, minNumSamplesForImportanceEstimation,
	// useImportanceEstimation);
	// }
	// }

	public HASCOFDWithParameterPruning(final Collection<Component> components,
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig,
			IObservableORGraphSearchFactory<TFDNode, String, V> searchFactory, Factory<? extends T> converter,
			String nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark, double importanceThreshold,
			int minNumSamplesForImportanceEstimation, boolean useParameterImportanceEstimation, SQLAdapter adapter) {
		super(components, paramRefinementConfig, new ForwardDecompositionHTNPlannerFactory<>(), searchFactory,
				new TFDSearchSpaceUtilFactory<>(), converter, nameOfRequiredInterface, benchmark, importanceThreshold,
				minNumSamplesForImportanceEstimation, useParameterImportanceEstimation, adapter);
	}

	public HASCOFDWithParameterPruning(final Collection<Component> components,
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig,
			Factory<? extends T> converter, String nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark,
			double importanceThreshold, int minNumSamplesForImportanceEstimation,
			boolean useParameterImportanceEstimation, SQLAdapter adapter) {
		this(components, paramRefinementConfig, new ORGraphSearchFactory<>(), converter, nameOfRequiredInterface,
				benchmark, importanceThreshold, minNumSamplesForImportanceEstimation, useParameterImportanceEstimation,
				adapter);
	}

	public HASCOFDWithParameterPruning(final Collection<Component> components,
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramRefinementConfig,
			Factory<? extends T> converter, String nameOfRequiredInterface, IObjectEvaluator<T, V> benchmark,
			OversearchAvoidanceConfig<TFDNode> oversearchAvoidanceConfig, double importanceThreshold,
			int minNumSamplesForImportanceEstimation, boolean useParameterImportanceEstimation, SQLAdapter adapter) {
		this(components, paramRefinementConfig,
				new UncertaintyORGraphSearchFactory<>(oversearchAvoidanceConfig, new CEOCTFDPathUnifier()), converter,
				nameOfRequiredInterface, benchmark, importanceThreshold, minNumSamplesForImportanceEstimation,
				useParameterImportanceEstimation, adapter);
		((UncertaintyORGraphSearchFactory<TFDNode, String, V>) getSearchFactory())
				.setSolutionEvaluator(getSolutionEvaluator());
	}
}