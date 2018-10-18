package autofe.algorithm.hasco;

import java.io.IOException;

import de.upb.crc901.mlplan.AbstractMLPlan;
import hasco.serialization.ComponentLoader;

public class MLPlanWithFeatureEngineering extends AbstractMLPlan<AutoFEWekaPipeline> {

	public MLPlanWithFeatureEngineering(final ComponentLoader componentLoader) throws IOException {
		super(componentLoader);
	}

}
