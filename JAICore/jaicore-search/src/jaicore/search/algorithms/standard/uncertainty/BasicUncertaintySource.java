package jaicore.search.algorithms.standard.uncertainty;

import java.util.List;
import jaicore.search.structure.core.Node;

public class BasicUncertaintySource<T> implements IUncertaintySource<T> {

	@Override
	public double calculateUncertainty(Node<T, UncertaintyFMeasure> n, List<T> solutionPath, List<UncertaintyFMeasure> simulationEvaluations) {
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
			for (UncertaintyFMeasure f : simulationEvaluations) {
				mean += f.getfValue();
			}
			mean /= simulationEvaluations.size();
			for (UncertaintyFMeasure f : simulationEvaluations) {
				sampleVariance += (f.getfValue() - mean) * (f.getfValue() - mean);
			}
			sampleVariance *= 1.0d / (solutionPath.size() - 1); 
			
			if (mean != 0.0d) {
				uncertainty *= 1 - (sampleVariance / mean);
			}
		}
		return uncertainty;
	}

}
