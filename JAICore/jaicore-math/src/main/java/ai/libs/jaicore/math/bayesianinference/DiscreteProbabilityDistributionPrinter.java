package ai.libs.jaicore.math.bayesianinference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.sets.LDSRelationComputer;
import ai.libs.jaicore.basic.sets.RelationComputationProblem;
import ai.libs.jaicore.logging.LoggerUtil;

public class DiscreteProbabilityDistributionPrinter {
	public String getTable(final DiscreteProbabilityDistribution d) {
		StringBuilder sb = new StringBuilder();

		/* create header */
		List<String> vars = new ArrayList<>(d.getVariables());
		for (String var : vars) {
			sb.append(var);
			sb.append(" | ");
		}
		sb.append(" P ");
		int len = sb.length();
		sb.append("\n");
		for (int i = 0; i < len; i++) {
			sb.append("-");
		}
		sb.append("\n");

		/* create one line for every combo */
		List<List<Integer>> binaryVector = new ArrayList<>();
		int n = vars.size();
		for (int i = 0; i < n; i++) {
			binaryVector.add(Arrays.asList(0, 1));
		}
		RelationComputationProblem<Integer> prob = new RelationComputationProblem<>(binaryVector);
		List<List<Integer>> combos;
		try {
			combos = new LDSRelationComputer<>(prob).call();
			for (List<Integer> truthVector : combos) {

				Set<String> activeVariables = new HashSet<>();
				for (int i = 0; i < n; i++) {
					int val = truthVector.get(i);
					if (val == 1) {
						activeVariables.add(vars.get(i));
					}
					sb.append(val + " | ");
				}
				sb.append(d.getProbabilities().get(activeVariables));
				sb.append("\n");
			}
			return sb.toString();
		} catch (AlgorithmTimeoutedException | AlgorithmExecutionCanceledException e) {
			return LoggerUtil.getExceptionInfo(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return LoggerUtil.getExceptionInfo(e);
		}

	}
}
