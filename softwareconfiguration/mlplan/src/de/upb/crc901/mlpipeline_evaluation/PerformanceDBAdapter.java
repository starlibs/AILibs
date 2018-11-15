package de.upb.crc901.mlpipeline_evaluation;

import com.google.common.base.Optional;

import hasco.model.ComponentInstance;
import jaicore.ml.cache.ReproducibleInstances;

/**
 * Database adapter for performance data
 * @author jmhansel
 *
 */
public class PerformanceDBAdapter {

	public PerformanceDBAdapter() {
		
	}

	
	public Optional<Double> exists(ComponentInstance composition, ReproducibleInstances reproducableInstances){
		double result = 0.0;
		Optional<Double> opt = Optional.of(result);
		return opt;
	}
	
	public void store(ComponentInstance composition, ReproducibleInstances reproducableInstances, double score) {
		
	}
	
}
