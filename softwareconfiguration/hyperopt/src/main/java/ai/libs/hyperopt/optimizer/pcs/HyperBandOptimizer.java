package ai.libs.hyperopt.optimizer.pcs;

/**
 *
 * @author kadirayk
 *
 */
public class HyperBandOptimizer extends AHBOptimizer {

	public HyperBandOptimizer(final PCSBasedOptimizerBuilder builder) {
		super(builder);
	}

	@Override
	public String getExecutableScript() {
		return "HpBandSterOptimizer.py";
	}

}
