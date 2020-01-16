package ai.libs.mlplan.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.Util;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class PreferenceBasedNodeEvaluator implements IPathEvaluator<TFDNode, String, Double>, ILoggingCustomizable {

	private final Collection<Component> components;
	private final List<String> orderingOfComponents;
	private Logger logger = LoggerFactory.getLogger(PreferenceBasedNodeEvaluator.class);
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
	public Double evaluate(final ILabeledPath<TFDNode, String> n) {
		this.logger.info("Received request for node evaluation.");
		List<String> appliedMethods = new LinkedList<>();
		for (TFDNode x : n.getNodes()) {
			if (x.getAppliedMethodInstance() != null) {
				appliedMethods.add(x.getAppliedMethodInstance().getMethod().getName());
			}
		}
		this.logger.debug("Determined {} applied methods: {}", appliedMethods.size(), appliedMethods);

		/* get partial component */
		ComponentInstance instance = Util.getSolutionCompositionFromState(this.components, n.getHead().getState(), false);
		boolean isPipeline = appliedMethods.stream().anyMatch(x -> x.toLowerCase().contains("pipeline"));
		boolean lastMethod = false;
		String classifierName = null;

		Double score = 0.0;
		this.logger.debug("The associated component instance is {}. Constitutes a pipeline? {}", instance, isPipeline ? "yes" : "no");
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
					double scoreOfParent;
					if ((scoreOfParent = ((BackPointerPath<TFDNode, String, Double>)n.getPathToParentOfHead()).getScore()) > 1.0e-6) {
						this.sentLogMessageForHavingEnteredSecondSubPhase = true;
						this.logger.info("Entering phase 1b! Breadth first search ends here, because the search is asking for the f-value of a node whose parent has been truely evaluated with an f-value of {}", scoreOfParent);
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

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
