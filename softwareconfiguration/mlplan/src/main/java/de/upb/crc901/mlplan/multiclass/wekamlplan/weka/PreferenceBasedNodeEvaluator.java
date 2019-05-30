package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;

public class PreferenceBasedNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

	private final Collection<Component> components;
	private final List<String> ORDERING_OF_CLASSIFIERS;
	private final static Logger logger = LoggerFactory.getLogger(PreferenceBasedNodeEvaluator.class);
	private boolean sentLogMessageForHavingEnteredSecondSubPhase = false;

	public PreferenceBasedNodeEvaluator(final Collection<Component> components, final List<String> ORDERING_OF_CLASSIFIERS) {
		super();
		this.components = components;
		this.ORDERING_OF_CLASSIFIERS = ORDERING_OF_CLASSIFIERS;
	}

	public PreferenceBasedNodeEvaluator(final Collection<Component> components) {
		this(components, new ArrayList<>());
	}

	@Override
	public Double f(final Node<TFDNode, ?> n) {
		List<String> appliedMethods = new LinkedList<>();
		for (TFDNode x : n.externalPath()) {
			if (x.getAppliedMethodInstance() != null) {
				appliedMethods.add(x.getAppliedMethodInstance().getMethod().getName());
			}
		}

		/* get partial component */
		ComponentInstance instance = Util.getSolutionCompositionFromState(this.components, n.getPoint().getState(), false);
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
				score *= 1.0e-10;
			} else {
				score = null;
				if (!this.sentLogMessageForHavingEnteredSecondSubPhase) {
					if ((Double) n.getParent().getInternalLabel() > 1.0e-6) {
						this.sentLogMessageForHavingEnteredSecondSubPhase = true;
						logger.info("Entering phase 1b! Breadth first search ends here, because the search is asking for the f-value of a node whose parent has been truely evaluated with an f-value of {}", n.getParent().getInternalLabel());
					}
				}
			}
		}
		return score;

	}

	@Override
	public String toString() {
		return "PreferenceBasedNodeEvaluator [ORDERING_OF_CLASSIFIERS=" + this.ORDERING_OF_CLASSIFIERS + "]";
	}
}
