package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;

public class PreferenceBasedNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

	private final Collection<Component> components;
	private final List<String> ORDERING_OF_CLASSIFIERS;

	public PreferenceBasedNodeEvaluator(final Collection<Component> components, final List<String> ORDERING_OF_CLASSIFIERS) {
		super();
		this.components = components;
		this.ORDERING_OF_CLASSIFIERS = ORDERING_OF_CLASSIFIERS;
	}

	@Override
	public Double f(final Node<TFDNode, ?> n) throws Exception {
		List<String> appliedMethods = new LinkedList<>();
		boolean last = false;
		for (TFDNode x : n.externalPath()) {
			if (x.getAppliedMethodInstance() != null) {
				appliedMethods.add(x.getAppliedMethodInstance().getMethod().getName());
				last = true;
			} else {
				last = false;
			}
		}

		/* get partial component */
		ComponentInstance instance = Util.getSolutionCompositionFromState(this.components, n.getPoint().getState(), false);

		if (instance != null) {
			ComponentInstance pp = instance.getSatisfactionOfRequiredInterfaces().get("preprocessor");
			if (pp != null && pp.getComponent().getName().contains("AttributeSelection")) {
				ComponentInstance search = pp.getSatisfactionOfRequiredInterfaces().get("search");
				ComponentInstance eval = pp.getSatisfactionOfRequiredInterfaces().get("eval");
				if (search != null && eval != null) {
					boolean isSetEvaluator = eval.getComponent().getName().toLowerCase().matches(".*(relief|gainratio|principalcomponents|onerattributeeval|infogainattributeeval|correlationattributeeval|symmetricaluncertattributeeval).*");
					boolean isRanker = search.getComponent().getName().toLowerCase().contains("ranker");
					boolean isNonRankerEvaluator = eval.getComponent().getName().toLowerCase().matches(".*(cfssubseteval).*");
//					if (isSetEvaluator && !isRanker) {
//						return 20000d;
//					}
//					if (isNonRankerEvaluator && isRanker) {
//						return 20000d;
//					}
					if (isSetEvaluator && !isRanker || isNonRankerEvaluator && isRanker)
						throw new IllegalArgumentException("The given combination of searcher and evaluator cannot be benchmarked since they are incompatible.");
				}
			}
		}

		boolean isPipeline = appliedMethods.stream().anyMatch(x -> x.toLowerCase().contains("pipeline"));
		boolean lastMethod = false;
		String classifierName = null;

		Double score = 0.0;
		if (instance != null) {
			if (instance.getComponent().getName().toLowerCase().contains("pipeline")) {
				lastMethod = lastMethod || appliedMethods.get(appliedMethods.size() - 1).startsWith("resolveBaseClassifierWith");

				if (instance.getSatisfactionOfRequiredInterfaces().containsKey("classifier")) {
					classifierName = instance.getSatisfactionOfRequiredInterfaces().get("classifier").getComponent().getName();
				} else {
					return 0.0;
				}
			} else {
				classifierName = instance.getComponent().getName();
				lastMethod = lastMethod || appliedMethods.get(appliedMethods.size() - 1).startsWith("resolveAbstractClassifierWith");
			}

			if (lastMethod) {
				if (isPipeline) {
					score += this.ORDERING_OF_CLASSIFIERS.size() + 1;
				}

				score += (this.ORDERING_OF_CLASSIFIERS.contains(classifierName) ? this.ORDERING_OF_CLASSIFIERS.indexOf(classifierName) + 1 : this.ORDERING_OF_CLASSIFIERS.size() + 1);
				score /= 100000;
			} else {
				score = null;
			}
		}

		return score;

	}

	@Override
	public String toString() {
		return "PreferenceBasedNodeEvaluator [ORDERING_OF_CLASSIFIERS=" + ORDERING_OF_CLASSIFIERS + "]";
	}
}
