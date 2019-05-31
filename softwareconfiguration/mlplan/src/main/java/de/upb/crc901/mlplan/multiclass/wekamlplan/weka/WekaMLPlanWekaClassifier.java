package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.io.IOException;

import de.upb.crc901.mlplan.core.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;

public class WekaMLPlanWekaClassifier extends MLPlanWekaClassifier {

	/**
	 * Automatically generated version uid for serialization.
	 */
	private static final long serialVersionUID = 985257791846750757L;

	public WekaMLPlanWekaClassifier(final MLPlanWekaBuilder builder) {
		super(builder);
	}

	public WekaMLPlanWekaClassifier() throws IOException {
		this(new MLPlanWekaBuilder());

	}
}
