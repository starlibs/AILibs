package cache;

import com.google.common.base.Optional;

/**
 * Database adapter for performance data
 * @author jmhansel
 *
 */
public class PerformanceDBAdapter {

	public PerformanceDBAdapter() {
		
	}

	
	public Optional<Double> exists(ComponentInstance composition, ReproducableInstances reproducableInstances){
		double result = 0.0;
		Optional<Double> opt = Optional.of(result);
		return opt;
	}
	
	public void store() {
		
	}
	
}
