package ai.libs.jaicore.math.bayesianinference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.graph.Graph;

public class VariableElimination extends ABayesianInferenceAlgorithm {

	private class Factor {
		private DiscreteProbabilityDistribution subDistribution;

		public Factor(final DiscreteProbabilityDistribution subDistribution) {
			super();
			this.subDistribution = subDistribution;
		}
	}

	private List<Factor> factors = new ArrayList<>();

	public VariableElimination(final BayesianInferenceProblem input) {
		super(input);
	}

	public List<String> preprocessVariables() {

		/* create a copy of the BN with which we want to work */
		Graph<String> reducedGraph = new Graph<>(this.net.getNet());

		/* remove irrelevant vars */
		boolean variableRemoved;
		do {
			variableRemoved = false;
			Collection<String> sinks = reducedGraph.getSinks();
			for (String sink : sinks) {
				if (!this.queryVariables.contains(sink) && !this.evidence.containsKey(sink)) {
					reducedGraph.removeItem(sink);
					variableRemoved = true;
				}
			}
		}
		while (variableRemoved);

		/* sort variables by order in network */
		List<String> vars = new ArrayList<>();
		while (!reducedGraph.isEmpty()) {
			Collection<String> sinks = reducedGraph.getSinks();
			for (String var : sinks) {
				vars.add(var);
				reducedGraph.removeItem(var);
			}
		}
		return vars;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		List<String> relevantAndOrderedVariables = this.preprocessVariables();
		for (String var : relevantAndOrderedVariables) {
			this.factors.add(this.makeFactor(var, this.evidence));
			if (this.hiddenVariables.contains(var)) {
				this.factors = this.sumOut(var, this.factors);
			}
		}
		this.setDistribution(this.multiply(this.factors).getNormalizedCopy());
		return null;
	}

	private Factor makeFactor(final String var, final Map<String, Boolean> evidence) throws InterruptedException {

		/* determine variables that are inputs for this factor (all non-evident parents and the variable itself if it is not evidence) */
		Collection<String> inputVariables = SetUtil.difference(this.net.getNet().getPredecessors(var), evidence.keySet());
		Set<String> trueEvidenceVariables = this.net.getNet().getPredecessors(var).stream().filter(k -> evidence.containsKey(k) && evidence.get(k)).collect(Collectors.toSet());
		boolean branchOverQueryVar = !evidence.keySet().contains(var);

		/* now compute the probabilities for all possible input combinations */
		DiscreteProbabilityDistribution factorDistribution = new DiscreteProbabilityDistribution();
		Collection<Collection<String>> factorEntries = SetUtil.powerset(inputVariables);
		for (Collection<String> event : factorEntries) {
			Set<String> eventWithEvidence = new HashSet<>(event);
			eventWithEvidence.addAll(trueEvidenceVariables);

			/* if the var is not part of the evidence */
			if (branchOverQueryVar) {
				double probWithPosVal = this.net.getProbabilityOfPositiveEvent(var, eventWithEvidence);
				double probWithNegVal = 1 - probWithPosVal;
				factorDistribution.addProbability(event, probWithNegVal);
				Set<String> eventWithPositiveVar = new HashSet<>(event);
				eventWithPositiveVar.add(var);
				factorDistribution.addProbability(eventWithPositiveVar, probWithPosVal);
			}

			/* the var is part of the evidence */
			else {
				double prob = -1;
				boolean wantPositiveProb = evidence.get(var);
				if (wantPositiveProb) {
					prob = this.net.getProbabilityOfPositiveEvent(var, eventWithEvidence);
				} else {
					prob = 1 - this.net.getProbabilityOfPositiveEvent(var, eventWithEvidence);
				}
				factorDistribution.addProbability(event, prob);
			}
		}
		return new Factor(factorDistribution);
	}

	private List<Factor> sumOut(final String var, final List<Factor> factors) throws InterruptedException {

		/* determine which factors will be eliminated and which stay */
		List<Factor> newFactors = new ArrayList<>();
		List<Factor> eliminatedFactors = new ArrayList<>();
		for (Factor f : factors) {
			if (!f.subDistribution.getVariables().contains(var)) {
				newFactors.add(f);
			} else {
				eliminatedFactors.add(f);
			}
		}

		/* first build point-wise product of distributions */
		DiscreteProbabilityDistribution productDistribution = eliminatedFactors.size() > 1 ? this.multiply(eliminatedFactors) : eliminatedFactors.get(0).subDistribution;

		/* compute distribution for elimination factor */
		DiscreteProbabilityDistribution distOfNewFactor = new DiscreteProbabilityDistribution();
		Collection<String> remainingVariablesInFactor = productDistribution.getVariables();
		remainingVariablesInFactor.remove(var);
		Collection<Collection<String>> entriesInReducedFactor = SetUtil.powerset(remainingVariablesInFactor);
		for (Collection<String> entry : entriesInReducedFactor) {
			Set<String> event = new HashSet<>(entry);
			double probForEventWithVariableIsNegative = productDistribution.getProbabilities().get(event);
			event.add(var);
			double probForEventWithVariableIsPositive = productDistribution.getProbabilities().get(event);
			event.remove(var);
			distOfNewFactor.addProbability(event, probForEventWithVariableIsNegative + probForEventWithVariableIsPositive);
		}
		newFactors.add(new Factor(distOfNewFactor));
		return newFactors;
	}

	public DiscreteProbabilityDistribution multiply(final Collection<Factor> factors) throws InterruptedException {
		DiscreteProbabilityDistribution current = null;
		for (Factor f : factors) {
			if (current != null) {
				current = this.multiply(current, f.subDistribution);
			} else {
				current = f.subDistribution;
			}
		}
		return current;
	}

	public DiscreteProbabilityDistribution multiply(final DiscreteProbabilityDistribution f1, final DiscreteProbabilityDistribution f2) throws InterruptedException {

		/* first determine variables that occur in the product */
		Set<String> variables = new HashSet<>();
		variables.addAll(f1.getVariables());
		variables.addAll(f2.getVariables());

		/* now compute the possible combinations of variables in the intersection (these will be the patterns to match each of the two tables) */
		List<String> intersectionVariables = new ArrayList<>(SetUtil.intersection(f1.getVariables(), f2.getVariables()));
		Collection<Collection<String>> commonVariableCombinations = SetUtil.powerset(intersectionVariables);
		Collection<String> otherVariables = SetUtil.difference(variables, intersectionVariables);
		Collection<Collection<String>> disjointVariableCombinations = SetUtil.powerset(otherVariables);

		/* for each combination of elements in the intersection, compute the set of matches in the first and in the second distribution */
		DiscreteProbabilityDistribution newDist = new DiscreteProbabilityDistribution();
		for (Collection<String> intersectionVarCombo : commonVariableCombinations) {
			for (Collection<String> differenceVarCombo : disjointVariableCombinations) {
				Set<String> eventInFirst = new HashSet<>(SetUtil.union(intersectionVarCombo, SetUtil.intersection(differenceVarCombo, f1.getVariables())));
				Set<String> eventInSecond = new HashSet<>(SetUtil.union(intersectionVarCombo, SetUtil.intersection(differenceVarCombo, f2.getVariables())));
				double p1 = f1.getProbabilities().get(eventInFirst);
				double p2 = f2.getProbabilities().get(eventInSecond);
				double p = p1 * p2;
				Set<String> jointEvent = new HashSet<>();
				jointEvent.addAll(intersectionVarCombo);
				jointEvent.addAll(differenceVarCombo);
				newDist.addProbability(jointEvent, p);
			}
		}
		return newDist;
	}
}