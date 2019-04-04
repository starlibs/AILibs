package jaicore.search.algorithms.standard.uncertainty;

import java.util.List;

import jaicore.search.model.travesaltree.Node;

public class BasicUncertaintySource<T, V extends Comparable<V>> implements IUncertaintySource<T, V> {

	@Override
	public double calculateUncertainty(Node<T, V> n, List<List<T>> simulationPaths, List<V> simulationEvaluations) {
		
		double uncertainty = 1.0d;
		
		if (simulationPaths != null && !simulationPaths.isEmpty()) {
			T t = n.getPoint();
			double meanDepth = 0.0d;
			for (List<T> path : simulationPaths) {
				if (path.contains(t)) {
					double post = 0.0d;
					boolean startsCounting = false;
					for (T pe : path) {
						if (startsCounting) {
							post++;
						}
						if (pe.equals(t)) {
							startsCounting = true;
						}
					}
					
					meanDepth += post / (double) path.size();
				}
			}
			if (meanDepth != 0.0d) {
				uncertainty = meanDepth / ((double) simulationPaths.size());
			}
		}
		
		if (simulationEvaluations != null && !simulationEvaluations.isEmpty() && simulationEvaluations.get(0) instanceof Double) {
			double mean = 0.0d;
			double sampleVariance = 0.0d;
			for (V f : simulationEvaluations) {
				mean += (Double)f;
			}
			mean /= simulationEvaluations.size();
			for (V f : simulationEvaluations) {
				sampleVariance += ((Double)f - mean) * ((Double)f - mean);
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
