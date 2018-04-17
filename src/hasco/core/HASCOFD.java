package hasco.core;

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
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearchFactory;

public class HASCOFD<T> extends HASCO<T, TFDNode, String, Double, ForwardDecompositionSolution> {
	
	private static class TFDSearchSpaceUtilFactory implements IHASCOSearchSpaceUtilFactory<TFDNode, String, Double> {

		@Override
		public IPathUnification<TFDNode> getPathUnifier() {
			return new CEOCTFDPathUnifier();
		}

		@Override
		public IPathToPlanConverter<TFDNode> getPathToPlanConverter() {
			return path -> path.stream().filter(n -> n.getAppliedAction() != null).map(n -> n.getAppliedAction()).collect(Collectors.toList());
		}
	}

	public HASCOFD(IObservableORGraphSearchFactory<TFDNode, String, Double> searchFactory, Factory<T> converter, INodeEvaluator<TFDNode,Double> nodeEvaluator,
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs, String nameOfRequiredInterface, IObjectEvaluator<T,Double> benchmark) {
		super(new ForwardDecompositionHTNPlannerFactory<>(), searchFactory, new TFDSearchSpaceUtilFactory(), nodeEvaluator, converter, paramConfigs, nameOfRequiredInterface, benchmark);
	}

	public HASCOFD(Factory<T> converter, INodeEvaluator<TFDNode,Double> nodeEvaluator, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs, String nameOfRequiredInterface, IObjectEvaluator<T,Double> benchmark) {
		this(new ORGraphSearchFactory<>(), converter, nodeEvaluator, paramConfigs, nameOfRequiredInterface, benchmark);
	}
}
