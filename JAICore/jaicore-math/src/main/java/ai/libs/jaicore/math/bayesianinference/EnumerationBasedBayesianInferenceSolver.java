package ai.libs.jaicore.math.bayesianinference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.sets.SetUtil;

public class EnumerationBasedBayesianInferenceSolver extends ABayesianInferenceAlgorithm {

	public EnumerationBasedBayesianInferenceSolver(final BayesianInferenceProblem input) {
		super(input);
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {

		/* get hidden variables */
		List<String> hiddenVariableOrder = new ArrayList<>(this.hiddenVariables);
		Map<String, Boolean> evidenceAssignment = new HashMap<>(this.evidence);

		/* create all combinations for query variables */
		Collection<Collection<String>> entries = SetUtil.powerset(this.queryVariables);
		for (Collection<String> positiveQueryVariables : entries) {
			Map<String, Boolean> initAssignment = new HashMap<>(evidenceAssignment);
			for (String var : this.queryVariables) {
				initAssignment.put(var, positiveQueryVariables.contains(var));
			}
			double prob = this.sumProbability(hiddenVariableOrder, 0, initAssignment);
			this.getDistribution().addProbability(positiveQueryVariables, prob);
		}
		return null;
	}

	public double sumProbability(final List<String> hiddenVariables, final int indexOfHiddenVariableToSum, final Map<String, Boolean> partialAssignment) {

		/* if the assignment is complete, calculate the prob from the network */
		Set<String> event = partialAssignment.keySet().stream().filter(partialAssignment::get).collect(Collectors.toSet());
		if (indexOfHiddenVariableToSum == hiddenVariables.size()) {
			double product = 1;
			for (String var : this.allModelVariables) {
				double factor = this.net.getProbabilityOfPositiveEvent(var, event);
				if (!event.contains(var)) {
					factor = 1 - factor;
				}
				product *= factor;
			}
			return product;
		}

		/* otherwise branch over the hidden variables */
		String branchVariable = hiddenVariables.get(indexOfHiddenVariableToSum);
		partialAssignment.put(branchVariable, false);
		double sum = 0;
		sum += this.sumProbability(hiddenVariables, indexOfHiddenVariableToSum + 1, partialAssignment);
		partialAssignment.put(branchVariable, true);
		sum += this.sumProbability(hiddenVariables, indexOfHiddenVariableToSum + 1, partialAssignment);
		return sum;
	}
}
