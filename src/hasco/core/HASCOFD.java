package hasco.core;

import java.util.Map;
import java.util.stream.Collectors;

import hasco.model.Component;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.query.Benchmark;
import hasco.query.Factory;
import jaicore.planning.algorithms.ForwardDecompositionHTNPlannerFactory;
import jaicore.planning.algorithms.IPathToPlanConverter;
import jaicore.planning.graphgenerators.task.ceoctfd.CEOCTFDPathUnifier;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.standard.core.ORGraphSearchFactory;

public class HASCOFD<T> extends HASCO<T, TFDNode, String, Double> {
	
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

	public HASCOFD(IObservableORGraphSearchFactory<TFDNode, String, Double> searchFactory, Factory<T> converter,
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs, String nameOfRequiredInterface, Benchmark<T,Double> benchmark) {
		super(new ForwardDecompositionHTNPlannerFactory<>(), searchFactory, new TFDSearchSpaceUtilFactory(), converter, paramConfigs, nameOfRequiredInterface, benchmark);
	}

	public HASCOFD(Factory<T> converter, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> paramConfigs, String nameOfRequiredInterface, Benchmark<T,Double> benchmark) {
		this(new ORGraphSearchFactory<>(), converter, paramConfigs, nameOfRequiredInterface, benchmark);
	}
}
