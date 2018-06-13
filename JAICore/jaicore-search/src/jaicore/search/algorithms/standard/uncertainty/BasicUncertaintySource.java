package jaicore.search.algorithms.standard.uncertainty;

import java.util.List;
import jaicore.search.structure.core.Node;

public class BasicUncertaintySource<T> implements IUncertaintySource<T, Double> {

	@Override
	public double calculateUncertainty(Node<T, ?> n, List<List<T>> simulationPaths, List<Double> simulationEvaluations) {
		
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
		
		if (simulationEvaluations != null && !simulationEvaluations.isEmpty()) {
			double mean = 0.0d;
			double sampleVariance = 0.0d;
			for (Double f : simulationEvaluations) {
				mean += f;
			}
			mean /= simulationEvaluations.size();
			for (Double f : simulationEvaluations) {
				sampleVariance += (f - mean) * (f - mean);
			}
			sampleVariance *= 1.0d / (simulationEvaluations.size() - 1); 
			
			if (mean != 0.0d) {
				uncertainty *= 1 - (sampleVariance / mean);
			}
		}
		return uncertainty;
	}

}
