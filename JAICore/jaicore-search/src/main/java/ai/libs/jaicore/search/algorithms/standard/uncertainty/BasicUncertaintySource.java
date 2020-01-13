package ai.libs.jaicore.search.algorithms.standard.uncertainty;

import java.util.List;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IUncertaintySource;
import org.api4.java.datastructure.graph.ILabeledPath;

public class BasicUncertaintySource<T, A, V extends Comparable<V>> implements IUncertaintySource<T, A,V> {

	@Override
	public double calculateUncertainty(final IEvaluatedPath<T, A, V> n, final List<ILabeledPath<T, A>> simulationPaths, final List<V> simulationEvaluations) {

		double uncertainty = 1.0d;

		if (simulationPaths != null && !simulationPaths.isEmpty()) {
			T t = n.getHead();
			double meanDepth = 0.0d;
			for (ILabeledPath<T, A> path : simulationPaths) {
				if (path.getNodes().contains(t) && !path.isPoint()) {
					double post = 0.0d;
					boolean startsCounting = false;
					for (T pe : path.getNodes()) {
						if (startsCounting) {
							post++;
						}
						if (pe.equals(t)) {
							startsCounting = true;
						}
					}

					meanDepth += post / path.getNumberOfNodes();
				}
			}
			if (meanDepth != 0.0d) {
				uncertainty = meanDepth / (simulationPaths.size());
			}
		}

		if (simulationEvaluations != null && simulationEvaluations.size() > 1
				&& simulationEvaluations.get(0) instanceof Double) {
			double mean = 0.0d;
			double sampleVariance = 0.0d;
			for (V f : simulationEvaluations) {
				mean += (Double) f;
			}
			mean /= simulationEvaluations.size();
			for (V f : simulationEvaluations) {
				sampleVariance += ((Double) f - mean) * ((Double) f - mean);
			}
			sampleVariance = Math.sqrt(sampleVariance / (simulationEvaluations.size() - 1));
			if (mean != 0.0d) {
				double coefficientOfVariation = sampleVariance / mean;
				coefficientOfVariation = Math.max(Math.abs(coefficientOfVariation), 1.0d);
				uncertainty *= coefficientOfVariation;
			}
		}
		return uncertainty;
	}

}
