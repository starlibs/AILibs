package jaicore.ea.algorithm.moea.moeaframework.util;

import java.util.Arrays;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

/**
 * Some utils for getting along with the MOEAFramework.
 *
 * @author wever
 */
public class MOEAFrameworkUtil {

	private MOEAFrameworkUtil() {
		// intentionally do nothing.
	}

	public static String solutionGenotypeToString(final Solution solution) {
		StringBuilder sb = new StringBuilder();
		sb.append(Arrays.toString(EncodingUtils.getInt(solution)));
		return sb.toString();
	}

	public static String solutionToString(final Solution solution) {
		StringBuilder sb = new StringBuilder();

		sb.append("Objectives: ");
		sb.append(Arrays.toString(solution.getObjectives()));
		sb.append("\t");

		sb.append("Annotations: ");
		sb.append(solution.getAttributes());

		return sb.toString();
	}

	public static String populationToString(final Population population) {
		StringBuilder sb = new StringBuilder();

		int solutionCounter = 1;
		for (Solution solution : population) {
			sb.append("Solution ");
			sb.append(solutionCounter++);
			sb.append(": ");
			sb.append(solutionToString(solution));
			sb.append("\n");
		}

		return sb.toString();
	}

}
