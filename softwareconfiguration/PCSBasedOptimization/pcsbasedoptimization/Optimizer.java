package ai.libs.hasco.pcsbasedoptimization;

/**
 * 
 * @author kadirayk
 *
 */
public interface Optimizer {

	public void optimize(String componentName) throws OptimizationException;
	
}
