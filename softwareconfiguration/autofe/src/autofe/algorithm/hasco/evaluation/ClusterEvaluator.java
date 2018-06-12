package autofe.algorithm.hasco.evaluation;

import java.util.Random;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;

// This is used for the search guidance
public class ClusterEvaluator<T> extends AbstractHASCOFEObjectEvaluator<T> {

	@Override
	public Double evaluate(FilterPipeline object) throws Exception {
		// TODO Auto-generated method stub
		return new Random().nextDouble() * 10;
	}

}
