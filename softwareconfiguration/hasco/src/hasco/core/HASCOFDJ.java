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

public class HASCOFDJ<T> extends HASCOJ<T, TFDNode, String, Double, ForwardDecompositionSolution> {

	public static class TFDSearchSpaceUtilFactory implements IHASCOSearchSpaceUtilFactory<TFDNode, String, Double> {

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

	public HASCOFDJ(IObservableORGraphSearchFactory<TFDNode, String, Double> searchFactory,
			Factory<? extends T> converter, INodeEvaluator<TFDNode, Double> nodeEvaluator,
			String nameOfRequiredInterface, IObjectEvaluator<T, Double> benchmark) {
		super(new ForwardDecompositionHTNPlannerFactory<>(), searchFactory, new TFDSearchSpaceUtilFactory(),
				nodeEvaluator, converter, nameOfRequiredInterface, benchmark, 0, 0, false);
	}

	/*
	 * Constructor with arguments for jmhansel parameter importance feature
	 */
	public HASCOFDJ(IObservableORGraphSearchFactory<TFDNode, String, Double> searchFactory,
			Factory<? extends T> converter, INodeEvaluator<TFDNode, Double> nodeEvaluator,
			String nameOfRequiredInterface, IObjectEvaluator<T, Double> benchmark, double importanceThreshold,
			int minNumSamplesForImportanceEstimation, boolean useImportanceEstimation) {
		super(new ForwardDecompositionHTNPlannerFactory<>(), searchFactory, new TFDSearchSpaceUtilFactory(),
				nodeEvaluator, converter, nameOfRequiredInterface, benchmark, importanceThreshold,
				minNumSamplesForImportanceEstimation, useImportanceEstimation);
	}

	public HASCOFDJ(Factory<? extends T> converter, INodeEvaluator<TFDNode, Double> nodeEvaluator,
			String nameOfRequiredInterface, IObjectEvaluator<T, Double> benchmark, double importanceThreshold,
			int minNumSamplesForImportanceEstimation, boolean useImportanceEstimation) {
		this(new ORGraphSearchFactory<>(), converter, nodeEvaluator, nameOfRequiredInterface, benchmark,
				importanceThreshold, minNumSamplesForImportanceEstimation, useImportanceEstimation);
	}
}
