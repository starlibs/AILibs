package ai.libs.mlplan.multiclass.wekamlplan.weka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.Util;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.model.travesaltree.Node;

public class PreferenceBasedNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

	private final Collection<Component> components;
	private final List<String> orderingOfComponents;
	private final static Logger logger = LoggerFactory.getLogger(PreferenceBasedNodeEvaluator.class);
	private boolean sentLogMessageForHavingEnteredSecondSubPhase = false;
	private String methodPrefix = "resolveAbstractClassifierWith";

	public PreferenceBasedNodeEvaluator(final Collection<Component> components, final List<String> orderingOfComponents) {
		super();
		this.components = components;
		this.orderingOfComponents = orderingOfComponents;
	}

	public PreferenceBasedNodeEvaluator(final Collection<Component> components) {
		this(components, new ArrayList<>());
	}

	public PreferenceBasedNodeEvaluator(final Collection<Component> components, final List<String> orderingOfComponents, final String methodPrefix) {
		this(components, orderingOfComponents);
		this.methodPrefix = methodPrefix;
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
				lastMethod = lastMethod || appliedMethods.get(appliedMethods.size() - 1).startsWith(this.methodPrefix);
			}

			if (lastMethod) {
				if (isPipeline) {
					score += this.orderingOfComponents.size() + 1;
				}

				score += (this.orderingOfComponents.contains(classifierName) ? this.orderingOfComponents.indexOf(classifierName) + 1 : this.orderingOfComponents.size() + 1);
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
		return "PreferenceBasedNodeEvaluator [ORDERING_OF_CLASSIFIERS=" + this.orderingOfComponents + "]";
	}
}
