package de.upb.crc901.automl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public class PreferenceBasedNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

	private final Collection<Component> components;
	private final List<String> ORDERING_OF_CLASSIFIERS;

	public PreferenceBasedNodeEvaluator(Collection<Component> components, List<String> oRDERING_OF_CLASSIFIERS) {
		super();
		this.components = components;
		ORDERING_OF_CLASSIFIERS = oRDERING_OF_CLASSIFIERS;
	}

	@Override
	public Double f(Node<TFDNode, ?> n) throws Throwable {

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
		ComponentInstance instance = Util.getSolutionCompositionFromState(components, n.getPoint().getState());

		if (instance != null) {
			ComponentInstance pp = instance.getSatisfactionOfRequiredInterfaces().get("preprocessor");
			if (pp != null && pp.getComponent().getName().contains("AttributeSelection")) {
				ComponentInstance search = pp.getSatisfactionOfRequiredInterfaces().get("search");
				ComponentInstance eval = pp.getSatisfactionOfRequiredInterfaces().get("eval");
				if (search != null && eval != null) {
					boolean isSetEvaluator = eval.getComponent().getName().toLowerCase().matches(
							".*(subseteval|relief|gainratio|principalcomponents|onerattributeeval|infogainattributeeval|correlationattributeeval|symmetricaluncertattributeeval).*");
					boolean isRanker = search.getComponent().getName().toLowerCase().contains("ranker");
					boolean isNonRankerEvaluator = eval.getComponent().getName().toLowerCase()
							.matches(".*(cfssubseteval).*");
					if (isSetEvaluator && !isRanker) {
						return 20000d;
					}
					if (isNonRankerEvaluator && isRanker) {
						return 20000d;
					}
				}
			}
		}

		if (!appliedMethods.isEmpty() && instance != null && last) {
			try {
				boolean isPipeline = appliedMethods.stream().anyMatch(x -> x.contains("pipeline"));
				boolean containsResolveClassifier = false;
				String resolvedClassifier = "";

				boolean lastMethod = false;
				if (isPipeline) {
					List<String> classifiers = appliedMethods.stream()
							.filter(x -> x.startsWith("resolveBaseClassifierWith")).collect(Collectors.toList());
					containsResolveClassifier = !classifiers.isEmpty();
					if (containsResolveClassifier) {
						resolvedClassifier = classifiers.get(0);
					}
					lastMethod = lastMethod
							|| appliedMethods.get(appliedMethods.size() - 1).startsWith("resolveBaseClassifierWith");
				} else {
					List<String> classifiers = appliedMethods.stream()
							.filter(x -> x.startsWith("resolveAbstractClassifierWith")).collect(Collectors.toList());
					containsResolveClassifier = !classifiers.isEmpty();
					if (containsResolveClassifier) {
						resolvedClassifier = classifiers.get(0);
					}
					lastMethod = lastMethod
							|| appliedMethods.get(appliedMethods.size() - 1).startsWith("resolveBaseClassifierWith");
				}
				boolean containsResolvePreprocessor = appliedMethods.stream()
						.anyMatch(x -> x.startsWith("resolveAbstractPreprocessorWith"));
				lastMethod = lastMethod
						|| appliedMethods.get(appliedMethods.size() - 1).startsWith("resolveAbstractPreprocessorWith");

				double score = 0d;
				if (isPipeline && !containsResolveClassifier) {
					return 0d;
				}

				if (lastMethod) {
					String classifierName = null;
					if (containsResolveClassifier) {
						classifierName = resolvedClassifier.substring(25);
					}
					if (isPipeline && containsResolveClassifier && containsResolvePreprocessor) {
						score += this.ORDERING_OF_CLASSIFIERS.size() + 1;
					}

					if (classifierName != null) {
						score += this.ORDERING_OF_CLASSIFIERS.indexOf(classifierName) + 1;
						return score / 100000;
					} else {
						return 0d;
					}
				} else {
					return null;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return 0d;
		}
	}
}
