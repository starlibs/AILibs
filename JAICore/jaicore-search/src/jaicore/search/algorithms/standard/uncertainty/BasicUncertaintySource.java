package jaicore.search.algorithms.standard.uncertainty;

import java.util.List;
import jaicore.search.structure.core.Node;

public class BasicUncertaintySource<T> implements IUncertaintySource<T, Double> {

	@Override
	public double calculateUncertainty(Node<T, ?> n, List<T> solutionPath, List<Double> simulationEvaluations) {
		T t = n.getPoint();
		double post = 0.0d;
		boolean startsCounting = false;
		for (T pe : solutionPath) {
			if (startsCounting) {
				post++;
			}
			if (pe.equals(t)) {
				startsCounting = true;
			}
		}
		
		double uncertainty = post / (double) solutionPath.size();
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
			sampleVariance *= 1.0d / (solutionPath.size() - 1); 
			
			if (mean != 0.0d) {
				uncertainty *= 1 - (sampleVariance / mean);
			}
		}
		return uncertainty;
	}

}
